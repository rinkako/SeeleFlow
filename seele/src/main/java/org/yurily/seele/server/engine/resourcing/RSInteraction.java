/*
 * Author : Rinka
 * Date   : 2020/1/29
 */
package org.yurily.seele.server.engine.resourcing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.GDP;
import org.yurily.seele.server.connect.rest.CallableSupervisor;
import org.yurily.seele.server.connect.rest.SupervisorRestPool;
import org.yurily.seele.server.connect.rest.SupervisorTelepathy;
import org.yurily.seele.server.connect.ws.ParticipantTelepathy;
import org.yurily.seele.server.engine.resourcing.allocator.Allocator;
import org.yurily.seele.server.engine.resourcing.context.ResourcingStateType;
import org.yurily.seele.server.engine.resourcing.context.TaskContext;
import org.yurily.seele.server.engine.resourcing.context.WorkitemContext;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantContext;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantPool;
import org.yurily.seele.server.engine.resourcing.principle.Principle;
import org.yurily.seele.server.engine.resourcing.queue.WorkQueue;
import org.yurily.seele.server.engine.resourcing.queue.WorkQueueContainer;
import org.yurily.seele.server.engine.resourcing.queue.WorkQueueType;
import org.yurily.seele.server.engine.resourcing.transition.*;
import org.yurily.seele.server.logging.RDBWorkitemLogger;
import org.yurily.seele.server.steady.seele.entity.SeeleItemlogEntity;
import org.yurily.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.yurily.seele.server.steady.seele.repository.SeeleItemlogRepository;
import org.yurily.seele.server.steady.seele.repository.SeeleRawtaskRepository;
import org.yurily.seele.server.steady.seele.repository.SeeleTaskRepository;
import org.yurily.seele.server.steady.seele.repository.SeeleWorkitemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class : RSInteraction
 * Usage : An encapsulation of resource service interactions. All resource service requests
 * from supervisors and participants will be handled here.
 */
@Slf4j
@Component
public class RSInteraction {

    @Autowired
    private WorkitemTransitionExecutor transitionExecutor;
    @Autowired
    private SupervisorTelepathy supervisorTelepathy;
    @Autowired
    private ParticipantTelepathy participantTelepathy;
    @Autowired
    private SeeleWorkitemRepository workitemRepository;
    @Autowired
    private SeeleItemlogRepository itemlogRepository;
    @Autowired
    private SeeleTaskRepository permanentTaskRepository;
    @Autowired
    private SeeleRawtaskRepository rawTaskRepository;

    /**
     * Handle workitem submit directly to RS.
     * <p>
     * This method MUST NOT TRANSACTIONAL, since it must ensure that the workitem insert
     * transaction has committed before notifying supervisor.
     */
    public WorkitemContext supervisorSubmitTask(TaskContext context) throws Exception {
        WorkitemContext workitem = this.createWorkitemTransactional(context);
        return allocateWorkitem(context, workitem, false);
    }

    /**
     * Reallocate a BAD_ALLOCATED workitem to a participant.
     *
     * @param workitem workitem to be allocated
     */
    public WorkitemContext reallocateBySupervisor(WorkitemContext workitem) throws Exception {
        TaskContext task = workitem.getTaskTemplate();
        return allocateWorkitem(task, workitem, true);
    }

    /**
     * Perform a resourcing action on an unoffered workitem, means workitem at CREATE or
     * BAD_ALLOCATED state. Seele will use Selector such as `Allocator` or `Supplier` to
     * allocate the workitem to a participant or offer the workitem to all candidates.
     * <p>
     * If there is no any satisfied participant candidate, the workitem will perform a
     * `BAD_ALLOCATED` transition. Thus a `BAD_ALLOCATED` workitem may stays at the same
     * state after reallocation.
     *
     * @param context      the task template for generated workitem
     * @param workitem     workitem to be handled
     * @param isReallocate whether this action is performed for reallocation
     */
    private WorkitemContext allocateWorkitem(TaskContext context, WorkitemContext workitem, boolean isReallocate) throws Exception {
        Principle principle = context.getPrinciple();
        // calculate candidate set
        Set<ParticipantContext> candidates;
        if (workitem.getSkill() == null) {
            log.info("no any skill requirement, candidate set will be all participants");
            candidates = ParticipantPool
                    .namespace(context.getNamespace())
                    .getParticipants();
        } else {
            candidates = ParticipantPool
                    .namespace(context.getNamespace())
                    .getSkilledParticipants(workitem.getSkill());
        }
        // bad allocation
        if (candidates.size() == 0) {
            log.error("Bad allocation occurred, WI is: " + workitem.toString());
            workitem.markBadAllocated();
        } else {
            // perform interaction
            switch (principle.getDispatchType()) {
                case ALLOCATE:
                    Allocator allocator = SelectorReflector.ReflectAllocator(principle.getDispatcherName());
                    ParticipantContext chosenOne = allocator.performAllocate(candidates, workitem);
                    WorkQueueContainer container = chosenOne.getQueueContainer();
                    container.setRepository(this.workitemRepository);
                    container.addToQueue(workitem, WorkQueueType.ALLOCATED);
                    if (isReallocate) {
                        WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Supervisor,
                                ResourcingStateType.BAD_ALLOCATED, ResourcingStateType.ALLOCATED, 0, new BaseTransitionCallback() {
                            @Override
                            public void onExecuted(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
                                int handlingCount = chosenOne.getHandlingWorkitemCount().incrementAndGet();
                                participantTelepathy.NotifyWorkitemAllocated(chosenOne, workitem);
                                log.info(String.format("Workitem %s(%s) is reallocated to participant [%s], with running workitem count: %s",
                                        workitem.getWid(), workitem.getTaskName(), chosenOne.getDisplayName(), handlingCount));
                            }
                        });
                        this.transitionExecutor.submit(workitem, transition);
                    } else {
                        WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Supervisor,
                                ResourcingStateType.CREATED, ResourcingStateType.ALLOCATED, 0);
                        this.transitionExecutor.submit(workitem, transition);
                        int handlingCount = chosenOne.getHandlingWorkitemCount().incrementAndGet();
                        this.participantTelepathy.NotifyWorkitemAllocated(chosenOne, workitem);
                        log.info(String.format("Supervisor `%s`(NS:%s) submitted raw task [%s] with %s, " +
                                        "Seele allocated it to participant [%s], with running workitem count: %s",
                                context.getRawEntity().getSubmitter(), context.getNamespace(), context.getRawEntity().getName(),
                                context.getPrinciple(), chosenOne.getDisplayName(), handlingCount));
                    }

                    break;
                case OFFER:
                    throw new Exception("NotImplemented");
            }
        }
        // notify supervisor
        this.notifySupervisorsWorkitemTransition(context.getRequestId(),
                context.getNamespace(), workitem, "NONE", null);
        return workitem;
    }

    /**
     * Supervisor requires the workitem to be `FORCE_COMPLETED` or `CANCELLED`, which are all
     * considered as FINAL state of a workitem indicates "supervisor makes sure this workitem
     * has already completed" and "supervisor makes sure this workitem has cancelled without
     * any affect".
     *
     * @param workitem workitem to be handled
     * @param isCancel whether a cancel request
     */
    @Transactional
    public TransitionRequestResult forceCompleteOrCancelWorkitemBySupervisor(WorkitemContext workitem, boolean isCancel) throws Exception {
        log.info(String.format("supervisor ask for force complete workitem [%s]", workitem.getWid()));
        String lastState = workitem.getState().name();
        ResourcingStateType target = isCancel ? ResourcingStateType.CANCELLED : ResourcingStateType.FORCE_COMPLETED;
        WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Supervisor,
                ResourcingStateType.ANY, target, -1, new BaseTransitionCallback() {
            @Override
            public void onPrepareExecute(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
                WorkQueue workQueue = workitem.getQueueReference();
                if (workQueue != null) {
                    workQueue.remove(workitem);
                }
                workitem.setCompleteTime(Timestamp.from(ZonedDateTime.now().toInstant()));
            }

            @Override
            public void onExecuted(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
                // notify supervisor
                notifySupervisorsWorkitemTransition(workitem.getRequestId(), workitem.getNamespace(), workitem, lastState, null);
                // complete the task
                workitem.getTaskTemplate().markAsFinish();
                log.info(String.format("%s workitem: %s(%s)", target.name(), workitem.getWid(), workitem.getTaskName()));
            }
        });
        return this.transitionExecutor.submit(workitem, transition);
    }

    /**
     * Create a workitem and flush steady MUST BE independently transactional execution.
     * Otherwise, transition requests may happen before this transaction commit and causes inconsistency.
     *
     * @param context Task template for workitem generation
     */
    @Transactional
    protected WorkitemContext createWorkitemTransactional(TaskContext context) throws Exception {
        return WorkitemContext.createFrom(context);
    }

    /**
     * Participant notified Seele that the workitem has already accepted by it,
     * but not start yet since concurrent control.
     *
     * @param epochId     participant notification mail epoch id
     * @param workitem    workitem to be handled
     * @param participant participant context
     */
    @Transactional
    public WorkitemContext acceptWorkitemByParticipant(int epochId, WorkitemContext workitem, ParticipantContext participant) throws Exception {
        Principle.DispatchType dispatchType = workitem.getPrinciple().getDispatchType();
        String lastState = workitem.getState().name();
        switch (dispatchType) {
            case ALLOCATE:
                WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Participant,
                        ResourcingStateType.ALLOCATED, ResourcingStateType.ACCEPTED, epochId, new BaseTransitionCallback() {
                    @Override
                    public void onPrepareExecute(WorkitemTransitionTracker tracker, WorkitemTransition transition) throws Exception {
                        WorkQueueContainer qContainer = participant.getQueueContainer();
                        workitem.setEnableTime(Timestamp.from(ZonedDateTime.now().toInstant()));
                        qContainer.moveAllocatedToAccepted(workitem);
                    }

                    @Override
                    public void onExecuted(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
                        // notify supervisor
                        notifySupervisorsWorkitemTransition(workitem.getRequestId(), workitem.getNamespace(), workitem, lastState, null);
                    }
                });
                this.transitionExecutor.submit(workitem, transition);
                break;
            case OFFER:
            default:
                log.error("unsupported dispatch type: " + dispatchType);
                break;
        }
        return workitem;
    }

    /**
     * Participant notified Seele that the workitem has already started and running on it.
     *
     * @param epochId     participant notification mail epoch id
     * @param workitem    workitem to be handled
     * @param participant participant context
     */
    @Transactional
    public WorkitemContext startWorkitemByParticipant(int epochId, WorkitemContext workitem, ParticipantContext participant) throws Exception {
        Principle.DispatchType dispatchType = workitem.getPrinciple().getDispatchType();
        String lastState = workitem.getState().name();
        switch (dispatchType) {
            case ALLOCATE:
                WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Participant,
                        ResourcingStateType.ACCEPTED, ResourcingStateType.RUNNING, epochId, new BaseTransitionCallback() {
                    @Override
                    public void onPrepareExecute(WorkitemTransitionTracker tracker, WorkitemTransition transition) throws Exception {
                        WorkQueueContainer qContainer = participant.getQueueContainer();
                        workitem.setStartTime(Timestamp.from(ZonedDateTime.now().toInstant()));
                        qContainer.moveAcceptedToStarted(workitem);
                    }

                    @Override
                    public void onExecuted(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
                        // notify supervisor
                        notifySupervisorsWorkitemTransition(workitem.getRequestId(), workitem.getNamespace(), workitem, lastState, null);
                    }
                });
                this.transitionExecutor.submit(workitem, transition);
                break;
            case OFFER:
            default:
                log.error("unsupported dispatch type: " + dispatchType);
                break;
        }
        return workitem;
    }

    /**
     * Participant notified Seele that the workitem has already completed on it. This request
     * will submit a final state transition to the transition executor, and after the transition
     * performed the workitem will be recognized as FINAL state workitem and ignores any requests
     * coming later.
     *
     * @param epochId      participant notification mail epoch id
     * @param workitem     workitem to be handled
     * @param participant  participant context
     * @param hasException whether the workitem completed with exception occurrence
     */
    @Transactional
    public WorkitemContext completeOrExceptionWorkitemByParticipant(int epochId, WorkitemContext workitem, ParticipantContext participant, boolean hasException) throws Exception {
        if (hasException) {
            log.warn("Workitem reported exception end: " + workitem.getWid());
        }
        Principle.DispatchType dispatchType = workitem.getPrinciple().getDispatchType();
        String lastState = workitem.getState().name();
        ResourcingStateType nextState = hasException ? ResourcingStateType.EXCEPTION : ResourcingStateType.COMPLETED;
        switch (dispatchType) {
            case ALLOCATE:
                WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Participant,
                        ResourcingStateType.RUNNING, nextState, epochId, new BaseTransitionCallback() {

                    @Override
                    public void onPrepareExecute(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
                        WorkQueueContainer qContainer = participant.getQueueContainer();
                        qContainer.removeFromQueue(workitem, WorkQueueType.STARTED);
                        workitem.setCompleteTime(Timestamp.from(ZonedDateTime.now().toInstant()));
                    }

                    @Override
                    public void onExecuted(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
                        if (workitem.isLogArrived()) {
                            workitem.markLogAlreadyFlushed();
                            try {
                                flushLogItem(workitem);
                            } catch (Exception e) {
                                log.error("Flush log for workitem fault, reset flush flag: " + e.getMessage());
                                workitem.markLogNotFlush();
                            }
                        } else {
                            log.info(String.format("Workitem %s, wait for cache GC to flush log", nextState.name()));
                        }
                        // notify supervisor
                        notifySupervisorsWorkitemTransition(workitem.getRequestId(), workitem.getNamespace(), workitem, lastState, null);
                        // complete the task
                        workitem.getTaskTemplate().markAsFinish();
                    }
                });
                this.transitionExecutor.submit(workitem, transition);
                break;
            case OFFER:
            default:
                log.error("unsupported dispatch type: " + dispatchType);
                break;
        }
        return workitem;
    }

    /**
     * Notify all supervisors in a namespace about a workitem has performed a state transition.
     *
     * @param requestId     request tracing id
     * @param namespace     namespace of supervisors
     * @param workitem      workitem which performed resourcing
     * @param prevStateName previous state name
     * @param payload       data payload to send to supervisors
     */
    private void notifySupervisorsWorkitemTransition(String requestId, String namespace, WorkitemContext workitem, String prevStateName, Object payload) {
        Map<String, CallableSupervisor> unmodifiedSupervisors = SupervisorRestPool.namespace(namespace).getAll();
        int size = unmodifiedSupervisors.size();
        log.info(String.format("Prepare to notify all supervisors(total: %s) about transitioning workitem %s, which type is: %s",
                size, workitem.getWid(), workitem.getTaskName()));
        for (CallableSupervisor cs : unmodifiedSupervisors.values()) {
            this.transitionReply(requestId, cs, workitem, prevStateName, payload);
        }
        log.info(String.format("Notified all %s supervisors in %s about resourcing workitem: %s",
                size, namespace, workitem.getWid()));
    }

    /**
     * Generate a transition notification mail and send to a supervisor.
     *
     * @param requestId     request tracing id
     * @param supervisor    supervisor to notify
     * @param workitem      workitem which performed resourcing
     * @param prevStateName previous state name
     * @param payload       data payload to send to supervisors
     */
    private void transitionReply(String requestId,
                                 CallableSupervisor supervisor,
                                 WorkitemContext workitem,
                                 String prevStateName,
                                 Object payload) {
        TransitionReply transitionReply = new TransitionReply();
        transitionReply.setWorkitemId(workitem.getWid());
        transitionReply.setWorkitemState(workitem.getState().name());
        transitionReply.setWorkitemPreviousState(prevStateName);
        transitionReply.setRequestId(requestId);
        transitionReply.setPayload(payload);
        this.supervisorTelepathy.callback(supervisor, transitionReply);
    }

    /**
     * Flush all run log to the log receiver such as RDB or Kafka.
     * This method is ONLY called after the workitem has finished.
     *
     * @param workitem workitem at final state
     */
    @Transactional
    public synchronized void flushLogItem(WorkitemContext workitem) {
        if (workitem.getLogContainer() instanceof RDBWorkitemLogger) {
            RDBWorkitemLogger rdbLogContainer = (RDBWorkitemLogger) workitem.getLogContainer();
            log.info("Using embedded logging, begin flush log with items: " + rdbLogContainer.size());
            String logContent = rdbLogContainer.dumpMultilineString();
            SeeleItemlogEntity logItem = new SeeleItemlogEntity();
            logItem.setWid(workitem.getWid());
            logItem.setFinished(true);
            logItem.setContent(logContent);
            this.itemlogRepository.save(logItem);
            rdbLogContainer.clear();
            log.info("Workitem log flushed");
        }
    }

    /**
     * Initialize the interaction controller.
     * <li> bind steady memory repository to static class fields
     * <li> load all workitems at active state from steady memory to memory cache
     */
    @PostConstruct
    private void init() {
        // binding repositories
        WorkitemContext.bindingRepository(this.workitemRepository);
        TaskContext.bindingRepository(this.permanentTaskRepository, this.rawTaskRepository);
        // load active workitem
        Set<String> activeSet = new HashSet<>();
        activeSet.add(ResourcingStateType.BAD_ALLOCATED.name());
        activeSet.add(ResourcingStateType.ALLOCATED.name());
        activeSet.add(ResourcingStateType.ACCEPTED.name());
        activeSet.add(ResourcingStateType.RUNNING.name());
        log.info("loading active workitem in steady");
        List<SeeleWorkitemEntity> activeEntities = this.workitemRepository.findAllByStateIn(activeSet);
        int succeedCount = 0;
        for (SeeleWorkitemEntity swe : activeEntities) {
            try {
                WorkitemContext.createFrom(swe);
                succeedCount++;
            } catch (Exception e) {
                log.error("load active workitem fault, ignored: " + e.getMessage());
            }
        }
        log.info(String.format("loaded active workitem in steady: %s/%s", succeedCount, activeEntities.size()));
    }

    @Data
    @ToString
    @EqualsAndHashCode
    public static class TransitionReply {
        @NotNull
        private String nodeId = GDP.SeeleId;

        @NotNull
        private String requestId;

        @NotNull
        private String workitemId;

        @NotNull
        private String workitemPreviousState;

        @NotNull
        private String workitemState;

        private Object payload;
    }
}
