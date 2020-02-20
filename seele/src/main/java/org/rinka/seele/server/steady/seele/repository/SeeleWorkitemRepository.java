/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.rinka.seele.server.steady.seele.repository;

import org.rinka.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Class : SeeleWorkitemRepository
 * Usage :
 */
public interface SeeleWorkitemRepository extends JpaRepository<SeeleWorkitemEntity, Long> {

    SeeleWorkitemEntity findByWid(String wid);

    List<SeeleWorkitemEntity> findAllByStateIn(Collection<String> candidates);
}
