/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.rinka.seele.server.service.internal;

import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class : WorkitemServiceImpl
 * Usage :
 */
@Component
public class WorkitemServiceImpl implements WorkitemService {

    /**
     * Force a workitem to complete by supervisor.
     * <p>
     * ForceComplete is a final state of workitem, means it will ignore
     * any transition request coming later.
     *
     * @param namespace workitem namespace
     * @param wid       workitem unique id
     */
    @Transactional
    @Override
    public WorkitemContext forceComplete(String namespace, String wid) {

        return null;
    }
}
