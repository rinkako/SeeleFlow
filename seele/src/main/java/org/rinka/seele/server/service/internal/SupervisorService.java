/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import org.rinka.seele.server.connect.rest.CallableSupervisor;

import java.util.Map;

/**
 * Class : SupervisorService
 * Usage :
 */
public interface SupervisorService {

    void registerSupervisor(String namespace, String supervisorId, String host, String callback, String fallback);

    boolean unregisterSupervisor(String namespace, String supervisorId);

    boolean clearNamespace(String namespace);

    CallableSupervisor get(String namespace);
}
