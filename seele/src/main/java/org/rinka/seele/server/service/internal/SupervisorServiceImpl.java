/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.connect.rest.CallableSupervisor;
import org.rinka.seele.server.connect.rest.SupervisorRestPool;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantPool;
import org.rinka.seele.server.engine.resourcing.queue.WorkQueueContainer;
import org.rinka.seele.server.engine.resourcing.queue.WorkQueueType;
import org.rinka.seele.server.steady.seele.entity.SeeleSupervisorEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleSupervisorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Class : SupervisorServiceImpl
 * Usage :
 */
@Slf4j
@Service
public class SupervisorServiceImpl implements SupervisorService {

    @Autowired
    private SeeleSupervisorRepository supervisorRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public boolean registerSupervisor(String namespace, String supervisorId, String host, String callback, String fallback) {
        SeeleSupervisorEntity fb = this.supervisorRepository.findByNamespaceAndSupervisorId(namespace, supervisorId);
        if (fb == null) {
            SeeleSupervisorEntity supervisorEntity = new SeeleSupervisorEntity();
            supervisorEntity.setNamespace(namespace);
            supervisorEntity.setCallbackUri(callback);
            supervisorEntity.setHost(host);
            supervisorEntity.setFallbackHost(fallback);
            supervisorEntity.setSupervisorId(supervisorId);
            this.supervisorRepository.save(supervisorEntity);
            SupervisorRestPool.namespace(namespace).add(supervisorId, host, callback, fallback);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    @Override
    public boolean unregisterSupervisor(String namespace, String supervisorId) {
        this.supervisorRepository.deleteAllByNamespaceAndSupervisorId(namespace, supervisorId);
        return SupervisorRestPool.namespace(namespace)
                .remove(supervisorId)
                .isPresent();
    }

    @Override
    public List<CallableSupervisor> listSupervisorsInNamespace(String namespace) {
        return new ArrayList<>(SupervisorRestPool.namespace(namespace).getSupervisors().values());
    }

    @Override
    public boolean clearNamespace(String namespace) {
        return SupervisorRestPool.remove(namespace)
                .isPresent();
    }

    @Override
    public CallableSupervisor get(String namespace) {
        return SupervisorRestPool.namespace(namespace).get().orElse(null);
    }

    @Override
    public List<ParticipantSummary> listParticipantsInNamespace(String namespace) {
        Set<ParticipantContext> participants = ParticipantPool.namespace(namespace).getParticipants();
        List<ParticipantSummary> result = new ArrayList<>(participants.size());
        for (ParticipantContext participant : participants) {
            ParticipantSummary summary = new ParticipantSummary();
            summary.setContext(participant);
            WorkQueueContainer container = participant.getQueueContainer();
            summary.setAllocatedWorkitems(new ArrayList<>(container.getQueue(WorkQueueType.ALLOCATED).getWorkitems().values()));
            summary.setAcceptedWorkitems(new ArrayList<>(container.getQueue(WorkQueueType.ACCEPTED).getWorkitems().values()));
            summary.setRunningWorkitems(new ArrayList<>(container.getQueue(WorkQueueType.STARTED).getWorkitems().values()));
            result.add(summary);
        }
        return result;
    }

    @PostConstruct
    void init() {
        log.info("Prepare reload supervisor from steady");
        List<SeeleSupervisorEntity> supervisors = this.supervisorRepository.findAll();
        for (SeeleSupervisorEntity se : supervisors) {
            SupervisorRestPool.namespace(se.getNamespace())
                    .add(se.getSupervisorId(), se.getHost(), se.getCallbackUri(), se.getFallbackHost());
        }
        log.info("Reloaded supervisor from steady, total: " + supervisors.size());
    }

    @Data
    @ToString
    @EqualsAndHashCode
    public static class ParticipantSummary {

        private List<WorkitemContext> allocatedWorkitems;
        private List<WorkitemContext> acceptedWorkitems;
        private List<WorkitemContext> runningWorkitems;

        private ParticipantContext context;
    }
}
