/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/20
 */
package org.rinka.seele.server.steady.seele.repository;

import org.rinka.seele.server.steady.seele.entity.SeeleTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeeleTaskRepository extends JpaRepository<SeeleTaskEntity, Long> {

    SeeleTaskEntity findByNamespaceAndName(String namespace, String name);
}
