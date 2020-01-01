/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.dao;

import org.rinka.seele.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Class : UserRepository
 * Usage :
 */
public interface UserRepository extends JpaRepository<User, Long> {

}
