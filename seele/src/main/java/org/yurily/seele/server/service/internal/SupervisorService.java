/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.yurily.seele.server.service.internal;

import org.yurily.seele.server.connect.rest.CallableSupervisor;

import java.util.List;

/**
 * Class : SupervisorService
 * Usage :
 */
public interface SupervisorService {

    boolean registerSupervisor(String namespace, String supervisorId, String host, String callback, String fallback);

    boolean unregisterSupervisor(String namespace, String supervisorId);

    List<CallableSupervisor> listSupervisorsInNamespace(String namespace);

    boolean clearNamespace(String namespace);

    CallableSupervisor get(String namespace);

    List<SupervisorServiceImpl.ParticipantSummary> listParticipantsInNamespace(String namespace);
}
