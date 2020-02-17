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
import org.rinka.seele.server.engine.resourcing.context.TaskContext;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantPool;
import org.rinka.seele.server.engine.resourcing.principle.Principle;
import org.rinka.seele.server.engine.resourcing.queue.WorkQueueContainer;
import org.rinka.seele.server.engine.resourcing.queue.WorkQueueType;
import org.rinka.seele.server.steady.seele.repository.SeeleWorkitemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Class : RSInteraction
 * Usage :
 */
@Slf4j
@Component
public class RSInteraction {

    @Autowired
    private SupervisorTelepathy supervisorTelepathy;

    @Autowired
    private ParticipantTelepathy participantTelepathy;

    @Autowired
    private SeeleWorkitemRepository workitemRepository;

    /**
     * Handle workitem submit directly to RS.
     *
     * This method MUST NOT TRANSACTIONAL, since it must ensure that the workitem insert
     * transaction has committed before notifying supervisor.
     */
    public WorkitemContext supervisorSubmitTask(TaskContext context) throws Exception {
        // generate workitem
        WorkitemContext workitem = this.createWorkitemTransactional(context);
        Principle principle = context.getPrinciple();
        String skillRequired = workitem.getSkill();
        // calculate candidate set
        Set<ParticipantContext> candidates = ParticipantPool
                .namespace(context.getNamespace())
                .getSkilledParticipants(skillRequired);
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
                    int handlingCount = chosenOne.getHandlingWorkitemCount().incrementAndGet();
                    this.participantTelepathy.NotifyWorkitemAllocated(chosenOne, workitem);
                    log.info(String.format("Supervisor `%s`(NS:%s) submitted raw task [%s] with %s, " +
                                    "Seele allocated it to participant [%s], with running workitem count: %s",
                            context.getSupervisorId(), context.getNamespace(), context.getTaskName(),
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
     * @param context
     * @return
     * @throws Exception
     */
    @Transactional
    protected WorkitemContext createWorkitemTransactional(TaskContext context) throws Exception {
        return WorkitemContext.createFrom(context, workitemRepository);
    }

    @Transactional
    public WorkitemContext acceptWorkitemByParticipant(WorkitemContext workitem, ParticipantContext participant) throws Exception {
        Principle.DispatchType dispatchType = workitem.getPrinciple().getDispatchType();
        switch (dispatchType) {
            case ALLOCATE:
                WorkQueueContainer qContainer = participant.getQueueContainer();
                qContainer.moveAllocatedToAccepted(workitem);
                workitem.setEnableTime(Timestamp.from(ZonedDateTime.now().toInstant()));
                break;
            case OFFER:
            default:
                log.error("unsupported dispatch type: " + dispatchType);
                break;
        }
        // notify supervisor
        this.notifySupervisorsWorkitemTransition(workitem.getRequestId(), workitem.getNamespace(), workitem, WorkitemContext.ResourcingStateType.ALLOCATED.name(), null);
        return workitem;
    }

    @Transactional
    public WorkitemContext startWorkitemByParticipant(WorkitemContext workitem, ParticipantContext participant) throws Exception {
        Principle.DispatchType dispatchType = workitem.getPrinciple().getDispatchType();
        switch (dispatchType) {
            case ALLOCATE:
                WorkQueueContainer qContainer = participant.getQueueContainer();
                qContainer.moveAcceptedToStarted(workitem);
                workitem.setStartTime(Timestamp.from(ZonedDateTime.now().toInstant()));
                break;
            case OFFER:
            default:
                log.error("unsupported dispatch type: " + dispatchType);
                break;
        }
        // notify supervisor
        this.notifySupervisorsWorkitemTransition(workitem.getRequestId(), workitem.getNamespace(), workitem, WorkitemContext.ResourcingStateType.ALLOCATED.name(), null);
        return workitem;
    }

    @Transactional
    public WorkitemContext completeWorkitemByParticipant(WorkitemContext workitem, ParticipantContext participant) throws Exception {
        Principle.DispatchType dispatchType = workitem.getPrinciple().getDispatchType();
        switch (dispatchType) {
            case ALLOCATE:
                WorkQueueContainer qContainer = participant.getQueueContainer();
                qContainer.removeFromQueue(workitem, WorkQueueType.STARTED);
                workitem.setState(WorkitemContext.ResourcingStateType.COMPLETED);
                workitem.setCompleteTime(Timestamp.from(ZonedDateTime.now().toInstant()));
                workitem.flushSteady();
                break;
            case OFFER:
            default:
                log.error("unsupported dispatch type: " + dispatchType);
                break;
        }
        log.info("workitem completed, remove cache");
        workitem.removeSelfFromCache();
        // notify supervisor
        this.notifySupervisorsWorkitemTransition(workitem.getRequestId(), workitem.getNamespace(), workitem, WorkitemContext.ResourcingStateType.ALLOCATED.name(), null);
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
