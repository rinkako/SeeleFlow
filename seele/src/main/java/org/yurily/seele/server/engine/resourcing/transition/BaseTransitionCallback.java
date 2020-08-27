/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.yurily.seele.server.engine.resourcing.transition;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public abstract class BaseTransitionCallback implements Serializable {
    private static final long serialVersionUID = 1L;

    public void onPrepareExecute(WorkitemTransitionTracker tracker, WorkitemTransition transition) throws Exception {
    }

    public abstract void onExecuted(WorkitemTransitionTracker tracker, WorkitemTransition transition);

    public void onFailed(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
        log.error(String.format("workitem transition failed: %s", transition.toString()));
    }

    public void onIgnored(WorkitemTransitionTracker tracker, WorkitemTransition transition) {
        log.warn(String.format("workitem transition duplication ignored: %s", transition.toString()));
    }

}
