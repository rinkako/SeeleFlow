/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.api;

import org.rinka.seele.server.api.form.ProcedureSubmitForm;
import org.rinka.seele.server.api.form.SupervisorRegisterForm;
import org.rinka.seele.server.api.form.SupervisorUnregisterForm;
import org.rinka.seele.server.api.response.SeeleRestResponse;
import org.rinka.seele.server.service.internal.SupervisorService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @PostMapping("/register")
    public SeeleRestResponse supervisorRegister(@Valid SupervisorRegisterForm body) {
        this.service.registerSupervisor(body.getNamespace(), body.getSupervisorId(),
                body.getHost(), body.getCallback(), body.getFallback());
        return SeeleRestResponse.ok();
    }

    @PostMapping("/unregister")
    public SeeleRestResponse supervisorUnregister(@Valid SupervisorUnregisterForm body) {
        this.service.unregisterSupervisor(body.getNamespace(), body.getSupervisorId());
        return SeeleRestResponse.ok();
    }

    @PostMapping("/procedure/submit")
    public SeeleRestResponse procedureSubmit(@Valid ProcedureSubmitForm body) {
        System.out.println(body);
        return SeeleRestResponse.ok(body);
    }
}
