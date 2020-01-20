/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Class : SupervisorServiceImpl
 * Usage :
 */
@Service
public class SupervisorServiceImpl implements SupervisorService {

    @Override
    public boolean registerSupervisor(String namespace, Map<String, String> supervisorDescriptor) {
        return false;
    }

    @Override
    public boolean unregisterSupervisor(String namespace, String supervisorId) {
        return false;
    }

    @Override
    public boolean clearNamespace(String namespace) {
        return false;
    }
}
