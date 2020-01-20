/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.api;

import org.rinka.seele.server.service.internal.SupervisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class : SupervisorController
 * Usage :
 */
@RestController
@RequestMapping("/api/rs/supervisor")
public class SupervisorController {

    private final SupervisorService service;

    public SupervisorController(SupervisorService service) {
        this.service = service;
    }


}
