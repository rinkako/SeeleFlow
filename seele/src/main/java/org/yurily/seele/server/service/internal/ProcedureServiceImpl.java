/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.yurily.seele.server.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.api.form.PrincipleForm;
import org.yurily.seele.server.engine.resourcing.RSInteraction;
import org.yurily.seele.server.engine.resourcing.context.WorkitemContext;
import org.yurily.seele.server.engine.resourcing.context.TaskContext;
import org.yurily.seele.server.engine.resourcing.principle.Principle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * Convert workitem submit directly request to resourcing interaction.
     *
     * This method MUST NOT TRANSACTIONAL, since it must ensure that the workitem insert
     * transaction has committed before notifying supervisor.
     *
     * @param requestId
     * @param namespace
     * @param supervisorId
     * @param taskName
     * @param principleDescriptor
     * @param skill
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public WorkitemContext submitDirectProcedureForResourcing(String requestId,
                                                              String namespace,
                                                              String supervisorId,
                                                              String taskName,
                                                              PrincipleForm principleDescriptor,
                                                              String skill,
                                                              Map<String, Object> args) throws Exception {

        TaskContext task = TaskContext.createRawFrom(requestId, namespace, supervisorId,
                taskName, Principle.of(principleDescriptor), skill, args);
        return this.interaction.supervisorSubmitTask(task);
    }

}
