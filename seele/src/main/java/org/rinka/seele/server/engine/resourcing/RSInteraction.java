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
import org.rinka.seele.server.engine.resourcing.allocator.Allocator;
import org.rinka.seele.server.engine.resourcing.context.TaskContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantPool;
import org.rinka.seele.server.engine.resourcing.principle.Principle;
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
    private SupervisorTelepathy telepathy;

    @Autowired
    private SeeleWorkitemRepository workitemRepository;

    @Transactional
    public Workitem supervisorSubmitTask(TaskContext context) throws Exception {
        // generate workitem
        Workitem workitem = Workitem.createFrom(context, workitemRepository);
        Principle principle = context.getPrinciple();
        String skillRequired = workitem.getSkill();
        // calculate candidate set
        Set<ParticipantContext> candidates = ParticipantPool
                .namespace(context.getNamespace())
                .getSkilledParticipants(skillRequired);
        // perform interaction
        switch (principle.getDispatchType()) {
            case ALLOCATE:
                Allocator allocator = SelectorReflector.ReflectAllocator(principle.getDispatcherName());
                ParticipantContext chosenOne = allocator.performAllocate(candidates, workitem);
                chosenOne.getQueueContainer().addToQueue(workitem, WorkQueueType.ALLOCATED);
                break;
            case OFFER:
                throw new Exception("NotImplemented");
        }
        // notify supervisor
        Optional<CallableSupervisor> supervisor = SupervisorRestPool
                .namespace(context.getNamespace())
                .get(context.getSupervisorId());
        if (supervisor.isPresent()) {
            CallableSupervisor callableSupervisor = supervisor.get();
            this.transitionReply(context.getRequestId(), callableSupervisor, workitem, "NONE", null);
        }
        return workitem;
    }

    private void transitionReply(String requestId,
                                 CallableSupervisor supervisor,
                                 Workitem workitem,
                                 String prevStateName,
                                 Object payload) {
        TransitionReply transitionReply = new TransitionReply();
        transitionReply.setWorkitemId(workitem.getWid());
        transitionReply.setWorkitemState(workitem.getState().name());
        transitionReply.setWorkitemPreviousState(prevStateName);
        transitionReply.setRequestId(requestId);
        transitionReply.setPayload(payload);
        this.telepathy.callback(supervisor, transitionReply);
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
