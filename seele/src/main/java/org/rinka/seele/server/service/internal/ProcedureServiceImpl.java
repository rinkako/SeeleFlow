/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.api.form.PrincipleForm;
import org.rinka.seele.server.engine.resourcing.RSInteraction;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.context.TaskContext;
import org.rinka.seele.server.engine.resourcing.principle.Principle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Class : ProcedureServiceImpl
 * Usage :
 */
@Slf4j
@Service
public class ProcedureServiceImpl implements ProcedureService {

    @Autowired
    private RSInteraction interaction;

    @Transactional
    @Override
    public WorkitemContext submitDirectProcedureForResourcing(String requestId,
                                                              String namespace,
                                                              String supervisorId,
                                                              String taskName,
                                                              PrincipleForm principleDescriptor,
                                                              String skill,
                                                              Map<String, Object> args) throws Exception {
        TaskContext task = new TaskContext();
        task.setNamespace(namespace);
        task.setTaskName(taskName);
        task.setArgs(args);
        task.setSkill(skill);
        task.setSupervisorId(supervisorId);
        task.setRequestId(requestId);
        task.setSubmitType(TaskContext.ResourcingTaskSubmitType.DIRECT);
        task.setPrinciple(Principle.of(principleDescriptor));
        return this.interaction.supervisorSubmitTask(task);
    }

}
