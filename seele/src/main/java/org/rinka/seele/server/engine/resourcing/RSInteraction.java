/*
 * Author : Rinka
 * Date   : 2020/1/29
 */
package org.rinka.seele.server.engine.resourcing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.GDP;
import org.rinka.seele.server.connect.rest.CallableSupervisor;
import org.rinka.seele.server.connect.rest.SupervisorRestPool;
import org.rinka.seele.server.connect.rest.SupervisorTelepathy;
import org.rinka.seele.server.connect.ws.ParticipantTelepathy;
import org.rinka.seele.server.engine.resourcing.allocator.Allocator;
import org.rinka.seele.server.engine.resourcing.context.ResourcingStateType;
import org.rinka.seele.server.engine.resourcing.context.TaskContext;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantPool;
import org.rinka.seele.server.engine.resourcing.principle.Principle;
import org.rinka.seele.server.engine.resourcing.queue.WorkQueue;
import org.rinka.seele.server.engine.resourcing.queue.WorkQueueContainer;
import org.rinka.seele.server.engine.resourcing.queue.WorkQueueType;
import org.rinka.seele.server.engine.resourcing.transition.*;
import org.rinka.seele.server.logging.RDBWorkitemLogger;
import org.rinka.seele.server.steady.seele.entity.SeeleItemlogEntity;
import org.rinka.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleItemlogRepository;
import org.rinka.seele.server.steady.seele.repository.SeeleRawtaskRepository;
import org.rinka.seele.server.steady.seele.repository.SeeleTaskRepository;
import org.rinka.seele.server.steady.seele.repository.SeeleWorkitemRepository;
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
 * Usage :
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
        // generate workitem
        WorkitemContext workitem = this.createWorkitemTransactional(context);
        Principle principle = context.getPrinciple();
        String skillRequired = workitem.getSkill();
        // calculate candidate set
        Set<ParticipantContext> candidates;
        if (skillRequired == null) {
            log.info("no any skill requirement, candidate set will be all participants");
            candidates = ParticipantPool
                    .namespace(context.getNamespace())
                    .getParticipants();
        } else {
            candidates = ParticipantPool
                    .namespace(context.getNamespace())
                    .getSkilledParticipants(skillRequired);
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
                    WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Supervisor, ResourcingStateType.CREATED, ResourcingStateType.ALLOCATED, 0);
                    this.transitionExecutor.submit(workitem, transition);
                    int handlingCount = chosenOne.getHandlingWorkitemCount().incrementAndGet();
                    this.participantTelepathy.NotifyWorkitemAllocated(chosenOne, workitem);
                    log.info(String.format("Supervisor `%s`(NS:%s) submitted raw task [%s] with %s, " +
                                    "Seele allocated it to participant [%s], with running workitem count: %s",
                            context.getRawEntity().getSubmitter(), context.getNamespace(), context.getRawEntity().getName(),
                            context.getPrinciple(), chosenOne.getDisplayName(), handlingCount));
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
     * Create a workitem and flush steady MUST BE independently transactional execution.
     * Otherwise, transition requests may happen before this transaction commit and causes inconsistency.
     */
    @Transactional
    protected WorkitemContext createWorkitemTransactional(TaskContext context) throws Exception {
        return WorkitemContext.createFrom(context);
    }

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

    @Transactional
    public WorkitemContext completeWorkitemByParticipant(int epochId, WorkitemContext workitem, ParticipantContext participant) throws Exception {
        Principle.DispatchType dispatchType = workitem.getPrinciple().getDispatchType();
        String lastState = workitem.getState().name();
        switch (dispatchType) {
            case ALLOCATE:
                WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Participant,
                        ResourcingStateType.RUNNING, ResourcingStateType.COMPLETED, epochId, new BaseTransitionCallback() {

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
                                log.error("flush log for workitem fault, reset flush flag: " + e.getMessage());
                                workitem.markLogNotFlush();
                            }
                        } else {
                            log.info("workitem completed, wait for cache GC to flush log");
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

    @Transactional
    public WorkitemContext forceCompleteWorkitemBySupervisor(WorkitemContext workitem) throws Exception {
        log.info(String.format("supervisor ask for force complete workitem [%s]", workitem.getWid()));
        String lastState = workitem.getState().name();
        WorkitemTransition transition = new WorkitemTransition(TransitionCallerType.Supervisor,
                ResourcingStateType.ANY, ResourcingStateType.FORCE_COMPLETED, -1, new BaseTransitionCallback() {
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
                log.info(String.format("Forced complete workitem: %s(%s)", workitem.getWid(), workitem.getTaskName()));
            }
        });
        this.transitionExecutor.submit(workitem, transition);
        return workitem;
    }

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

    @Transactional
    public synchronized void flushLogItem(WorkitemContext workitem) {
        if (workitem.getLogContainer() instanceof RDBWorkitemLogger) {
            RDBWorkitemLogger rdbLogContainer = (RDBWorkitemLogger) workitem.getLogContainer();
            log.info("using embedded logging, begin flush log with items: " + rdbLogContainer.size());
            String logContent = rdbLogContainer.dumpMultilineString();
            SeeleItemlogEntity logItem = new SeeleItemlogEntity();
            logItem.setWid(workitem.getWid());
            logItem.setFinished(true);
            logItem.setContent(logContent);
            this.itemlogRepository.save(logItem);
            rdbLogContainer.clear();
            log.info("workitem log flushed");
        }
    }

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
