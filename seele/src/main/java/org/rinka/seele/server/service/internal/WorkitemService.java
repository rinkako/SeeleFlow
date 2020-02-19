/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.rinka.seele.server.service.internal;

import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;

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
    WorkitemContext forceComplete(String namespace, String wid);
}
