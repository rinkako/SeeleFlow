/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.yurily.seele.server.steady.seele.repository;

import org.yurily.seele.server.steady.seele.entity.SeeleSupervisorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Class : SeeleSupervisorRepository
 * Usage :
 */
public interface SeeleSupervisorRepository extends JpaRepository<SeeleSupervisorEntity, Long> {

    SeeleSupervisorEntity findByNamespaceAndSupervisorId(String namespace, String supervisorId);

    void deleteAllByNamespaceAndSupervisorId(String namespace, String supervisorId);
}
