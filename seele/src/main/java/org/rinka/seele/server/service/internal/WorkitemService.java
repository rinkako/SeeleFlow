/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.rinka.seele.server.service.internal;

import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.transition.TransitionRequestResult;

public interface WorkitemService {

    /**
     * Force a workitem to complete by supervisor.
     * <p>
     * ForceComplete is a final state of workitem, means it will ignore
     * any transition request coming later.
     *
     * @param namespace workitem namespace
     * @param wid       workitem unique id
     */
    TransitionRequestResult forceComplete(String namespace, String wid) throws Exception;

    /**
     * Force a workitem to be cancelled by supervisor.
     * <p>
     * Cancelled is a final state of workitem, means it will ignore
     * any transition request coming later.
     *
     * @param namespace workitem namespace
     * @param wid       workitem unique id
     */
    TransitionRequestResult forceCancel(String namespace, String wid) throws Exception;

    /**
     * Reallocate a workitem at BAD_ALLOCATED state to candidate participants.
     *
     * @param namespace workitem namespace
     * @param wid       workitem unique id
     */
    WorkitemContext reallocate(String namespace, String wid) throws Exception;
}
