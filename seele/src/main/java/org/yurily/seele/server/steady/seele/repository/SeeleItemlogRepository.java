/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/18
 */
package org.yurily.seele.server.steady.seele.repository;

import org.yurily.seele.server.steady.seele.entity.SeeleItemlogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeeleItemlogRepository extends JpaRepository<SeeleItemlogEntity, Long> {

    SeeleItemlogEntity findByWid(String wid);
}
