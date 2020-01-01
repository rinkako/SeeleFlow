/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.service;

import org.rinka.seele.server.entity.User;

import java.util.List;

public interface UserAuthService {
    public List<User> getAllValidUser();
}
