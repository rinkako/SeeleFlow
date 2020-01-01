/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.service;

import org.rinka.seele.server.dao.UserRepository;
import org.rinka.seele.server.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class : UserAuthServiceImpl
 * Usage :
 */
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;

    public UserAuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllValidUser() {
        return this.userRepository.findAll();
    }
}
