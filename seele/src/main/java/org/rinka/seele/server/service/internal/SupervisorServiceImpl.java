/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import org.rinka.seele.server.connect.rest.CallableSupervisor;
import org.rinka.seele.server.connect.rest.SupervisorRestPool;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Class : SupervisorServiceImpl
 * Usage :
 */
@Service
public class SupervisorServiceImpl implements SupervisorService {

    @Override
    public void registerSupervisor(String namespace, String supervisorId, String host, String callback, String fallback) {
        SupervisorRestPool.namespace(namespace).add(supervisorId, host, callback, fallback);
    }

    @Override
    public boolean unregisterSupervisor(String namespace, String supervisorId) {
        return SupervisorRestPool.namespace(namespace)
                .remove(supervisorId)
                .isPresent();
    }

    @Override
    public boolean clearNamespace(String namespace) {
        return SupervisorRestPool.remove(namespace)
                .isPresent();
    }

    public CallableSupervisor get(String namespace) {
        return SupervisorRestPool.namespace(namespace).get().orElse(null);
    }
}
