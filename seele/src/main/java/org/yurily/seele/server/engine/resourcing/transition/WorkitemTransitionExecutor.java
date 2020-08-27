/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.yurily.seele.server.engine.resourcing.transition;

import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.engine.resourcing.context.WorkitemContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : WorkitemTransitionExecutor
 * Usage : All workitem state transition should submit to this executor
 * NEVER modify the workitem state outside transition management.
 */
@Slf4j
@Component
public final class WorkitemTransitionExecutor {

    private final ConcurrentHashMap<String, WorkitemTransitionTracker> workitemTrackerPool = new ConcurrentHashMap<>();

    /**
     * Request a workitem to change its state according to the Transition descriptor.
     *
     * @param workitem   workitem to perform transition
     * @param transition transition action descriptor
     */
    public TransitionRequestResult submit(WorkitemContext workitem, WorkitemTransition transition) throws Exception {
        WorkitemTransitionTracker tracker = this.workitemTrackerPool.computeIfAbsent(workitem.getWid(), wid -> new WorkitemTransitionTracker(workitem));
        return tracker.transitionTo(transition);
    }

    /**
     * Delete a tracker, this method only call when the workitem completed and
     * flushed all data to steady.
     *
     * @param workitem archiving workitem
     */
    public void removeTracker(WorkitemContext workitem) {
        this.workitemTrackerPool.remove(workitem.getWid());
    }
}
