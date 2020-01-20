/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import java.util.Map;

/**
 * Class : SupervisorService
 * Usage :
 */
public interface SupervisorService {

    boolean registerSupervisor(String namespace, Map<String, String> supervisorDescriptor);

    boolean unregisterSupervisor(String namespace, String supervisorId);

    boolean clearNamespace(String namespace);
}
