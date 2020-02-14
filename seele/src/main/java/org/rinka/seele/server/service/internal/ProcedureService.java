/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.service.internal;

import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;

import java.util.Map;

/**
 * Class : ProcedureService
 * Usage :
 */
public interface ProcedureService {
    WorkitemContext submitDirectProcedureForResourcing(String requestId,
                                                       String namespace,
                                                       String supervisorId,
                                                       String taskName,
                                                       String principleDescriptor,
                                                       String skill,
                                                       Map<String, Object> args) throws Exception;
}
