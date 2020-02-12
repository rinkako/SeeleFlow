/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.rinka.seele.server.api.form.NamespaceForm;
import org.rinka.seele.server.api.form.ProcedureSubmitForm;
import org.rinka.seele.server.api.form.SupervisorRegisterForm;
import org.rinka.seele.server.api.form.SupervisorUnregisterForm;
import org.rinka.seele.server.api.response.SeeleRestResponse;
import org.rinka.seele.server.service.internal.ProcedureService;
import org.rinka.seele.server.service.internal.SupervisorService;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Class : SupervisorController
 * Usage :
 */
@RestController
@RequestMapping("/api/rs/supervisor")
public class SupervisorController {

    @Autowired
    private SupervisorService supervisorService;
    @Autowired
    private ProcedureService procedureService;

    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public SeeleRestResponse supervisorRegister(@Valid SupervisorRegisterForm body) {
        this.supervisorService.registerSupervisor(body.getNamespace(), body.getSupervisorId(),
                body.getHost(), body.getCallback(), body.getFallback());
        return SeeleRestResponse.ok();
    }

    @ResponseBody
    @RequestMapping(value = "/unregister", method = RequestMethod.POST)
    public SeeleRestResponse supervisorUnregister(@Valid SupervisorUnregisterForm body) {
        this.supervisorService.unregisterSupervisor(body.getNamespace(), body.getSupervisorId());
        return SeeleRestResponse.ok();
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public SeeleRestResponse listSupervisors(@Valid NamespaceForm body) {
        Object r = this.supervisorService.listSupervisorsInNamespace(body.getNamespace());
        return SeeleRestResponse.ok(r);
    }

    @ResponseBody
    @RequestMapping(value = "/procedure/submit", method = RequestMethod.POST)
    public SeeleRestResponse procedureSubmit(@Valid ProcedureSubmitForm body) throws Exception {
        Map args = JsonUtil.parse(body.getArgs(), Map.class);
        Object item = this.procedureService.submitDirectProcedureForResourcing(body.getRequestId(), body.getNamespace(),
                body.getSupervisorId(), body.getTaskName(), body.getPrincipleDescriptor(),
                body.getSkillRequirement(), args);
        return SeeleRestResponse.ok(item);
    }
}
