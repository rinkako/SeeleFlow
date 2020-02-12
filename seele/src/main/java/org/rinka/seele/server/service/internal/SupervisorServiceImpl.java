/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.connect.rest.CallableSupervisor;
import org.rinka.seele.server.connect.rest.SupervisorRestPool;
import org.rinka.seele.server.steady.seele.entity.SeeleSupervisorEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleSupervisorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


/**
 * Class : SupervisorServiceImpl
 * Usage :
 */
@Slf4j
@Service
public class SupervisorServiceImpl implements SupervisorService {

    @Autowired
    private SeeleSupervisorRepository supervisorRepository;

    @Transactional
    @Override
    public void registerSupervisor(String namespace, String supervisorId, String host, String callback, String fallback) {
        SeeleSupervisorEntity supervisorEntity = new SeeleSupervisorEntity();
        supervisorEntity.setNamespace(namespace);
        supervisorEntity.setCallbackUri(callback);
        supervisorEntity.setHost(host);
        supervisorEntity.setFallbackHost(fallback);
        supervisorEntity.setSupervisorId(supervisorId);
        this.supervisorRepository.save(supervisorEntity);
        SupervisorRestPool.namespace(namespace).add(supervisorId, host, callback, fallback);
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

    public CallableSupervisor get(String namespace) {
        return SupervisorRestPool.namespace(namespace).get().orElse(null);
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
}
