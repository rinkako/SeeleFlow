/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.api;

import org.rinka.seele.server.api.form.*;
import org.rinka.seele.server.api.response.SeeleRestResponse;
import org.rinka.seele.server.service.internal.ProcedureService;
import org.rinka.seele.server.service.internal.SupervisorService;
import org.rinka.seele.server.service.internal.WorkitemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    @Autowired
    private WorkitemService workitemService;

    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public SeeleRestResponse supervisorRegister(@Valid @RequestBody SupervisorRegisterForm body) {
        boolean existFlag = this.supervisorService.registerSupervisor(body.getNamespace(), body.getSupervisorId(),
                body.getHost(), body.getCallback(), body.getFallback());
        return SeeleRestResponse.ok(existFlag);
    }

    @ResponseBody
    @RequestMapping(value = "/unregister", method = RequestMethod.POST)
    public SeeleRestResponse supervisorUnregister(@Valid @RequestBody SupervisorUnregisterForm body) {
        boolean existFlag = this.supervisorService.unregisterSupervisor(body.getNamespace(), body.getSupervisorId());
        return SeeleRestResponse.ok(existFlag);
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public SeeleRestResponse listSupervisors(@Valid @RequestBody NamespaceForm body) {
        Object r = this.supervisorService.listSupervisorsInNamespace(body.getNamespace());
        return SeeleRestResponse.ok(r);
    }

    @ResponseBody
    @RequestMapping(value = "/procedure/submit", method = RequestMethod.POST)
    public SeeleRestResponse procedureSubmit(@Valid @RequestBody ProcedureSubmitForm body) throws Exception {
        Object item = this.procedureService.submitDirectProcedureForResourcing(body.getRequestId(), body.getNamespace(),
                body.getSupervisorId(), body.getTaskName(), body.getPrinciple(),
                body.getSkillRequirement(), body.getArgs());
        return SeeleRestResponse.ok(item);
    }

    @ResponseBody
    @RequestMapping(value = "/workitem/forceComplete", method = RequestMethod.POST)
    public SeeleRestResponse forceComplete(@Valid @RequestBody WidForm body) throws Exception {
        Object result = this.workitemService.forceComplete(body.getNamespace(), body.getWid());
        return SeeleRestResponse.ok(result);
    }

    @ResponseBody
    @RequestMapping(value = "/workitem/cancel", method = RequestMethod.POST)
    public SeeleRestResponse forceCancel(@Valid @RequestBody WidForm body) throws Exception {
        Object result = this.workitemService.forceCancel(body.getNamespace(), body.getWid());
        return SeeleRestResponse.ok(result);
    }
}
