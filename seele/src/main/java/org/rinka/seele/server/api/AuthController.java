/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class : AuthController
 * Usage :
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @RequestMapping(value="/")
    public String gateway() {
        return "Seele: you try to access auth gateway.";
    }
}
