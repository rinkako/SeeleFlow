/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/20
 */
package org.rinka.seele.server.steady.seele.repository;

import org.rinka.seele.server.steady.seele.entity.SeeleRawtaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Class : SeeleRawtaskRepository
 * Usage :
 */
public interface SeeleRawtaskRepository extends JpaRepository<SeeleRawtaskEntity, Long> {

    SeeleRawtaskEntity findByRequestId(String requestId);

    SeeleRawtaskEntity findByNamespaceAndName(String namespace, String name);
}
