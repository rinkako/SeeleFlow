/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.yurily.seele.server.service.internal;

import org.yurily.seele.server.engine.resourcing.RSInteraction;
import org.yurily.seele.server.engine.resourcing.context.WorkitemContext;
import org.yurily.seele.server.engine.resourcing.transition.TransitionRequestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class : WorkitemServiceImpl
 * Usage :
 */
@Component
public class WorkitemServiceImpl implements WorkitemService {

    @Autowired
    private RSInteraction interaction;

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
    public TransitionRequestResult forceComplete(String namespace, String wid) throws Exception {
        WorkitemContext workitem = WorkitemContext.loadByNamespaceAndWid(namespace, wid);
        return this.interaction.forceCompleteOrCancelWorkitemBySupervisor(workitem, false);
    }

    /**
     * Force a workitem to be cancelled by supervisor.
     * <p>
     * Cancelled is a final state of workitem, means it will ignore
     * any transition request coming later.
     *
     * @param namespace workitem namespace
     * @param wid       workitem unique id
     */
    @Override
    public TransitionRequestResult forceCancel(String namespace, String wid) throws Exception {
        WorkitemContext workitem = WorkitemContext.loadByNamespaceAndWid(namespace, wid);
        return this.interaction.forceCompleteOrCancelWorkitemBySupervisor(workitem, true);
    }

    /**
     * Reallocate a workitem at BAD_ALLOCATED state to candidate participants.
     *
     * @param namespace workitem namespace
     * @param wid       workitem unique id
     */
    @Override
    public WorkitemContext reallocate(String namespace, String wid) throws Exception {
        WorkitemContext workitem = WorkitemContext.loadByNamespaceAndWid(namespace, wid);
        return this.interaction.reallocateBySupervisor(workitem);
    }
}
