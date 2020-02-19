/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.rinka.seele.server.engine.resourcing.transition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTransitionCallback {

    public abstract void onExecuted(WorkitemTransitionTracker tracker, WorkitemTransition transition);

    public void onFailed(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
        log.error(String.format("workitem transition failed: %s", transition.toString()));
    }

    public void onIgnored(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
        log.error(String.format("workitem transition duplication ignored: %s", transition.toString()));
    }

}
