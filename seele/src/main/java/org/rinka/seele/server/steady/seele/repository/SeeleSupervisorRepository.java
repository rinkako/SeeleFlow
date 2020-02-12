/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.rinka.seele.server.steady.seele.repository;

import org.rinka.seele.server.steady.seele.entity.SeeleSupervisorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Class : SeeleSupervisorRepository
 * Usage :
 */
public interface SeeleSupervisorRepository extends JpaRepository<SeeleSupervisorEntity, Long> {
    void deleteAllByNamespaceAndSupervisorId(String namespace, String supervisorId);
}
