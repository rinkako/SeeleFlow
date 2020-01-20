/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.api;

import org.rinka.seele.server.entity.User;
import org.rinka.seele.server.service.web.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Class : AuthController
 * Usage :
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    UserAuthService service;

    @RequestMapping(value="/")
    public String gateway() {
        return "Seele: you try to access auth gateway.";
    }

    @RequestMapping(value="/user/get_all")
    public String getAllUsers() {
        List<User> users = this.service.getAllValidUser();
        users.forEach(u -> System.out.println(u.toString()));
        return "200";
    }
}
