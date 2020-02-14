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

    @Transactional
    public WorkitemContext supervisorSubmitTask(TaskContext context) throws Exception {
        // generate workitem
        WorkitemContext workitem = WorkitemContext.createFrom(context, workitemRepository);
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
        log.info(String.format("Prepare to notify supervisor `%s`(NS:%s) about resourcing",
                context.getSupervisorId(), context.getNamespace()));
        Optional<CallableSupervisor> supervisor = SupervisorRestPool
                .namespace(context.getNamespace())
                .get(context.getSupervisorId());
        if (supervisor.isPresent()) {
            CallableSupervisor callableSupervisor = supervisor.get();
            this.transitionReply(context.getRequestId(), callableSupervisor, workitem, "NONE", null);
        }
        log.info(String.format("Notified supervisor `%s`(NS:%s) about resourcing",
                context.getSupervisorId(), context.getNamespace()));
        return workitem;
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
