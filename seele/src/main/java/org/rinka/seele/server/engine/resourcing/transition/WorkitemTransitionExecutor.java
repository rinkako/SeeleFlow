/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.rinka.seele.server.engine.resourcing.transition;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : WorkitemTransitionExecutor
 * Usage :
 */
@Slf4j
@Component
public final class WorkitemTransitionExecutor {

    private final ConcurrentHashMap<String, WorkitemTransitionTracker> workitemTrackerPool = new ConcurrentHashMap<>();

    public TransitionRequestResult submit(WorkitemContext workitem, WorkitemTransition transition) throws Exception {
        WorkitemTransitionTracker tracker = this.workitemTrackerPool.computeIfAbsent(workitem.getWid(), wid -> new WorkitemTransitionTracker(workitem));
        return tracker.transitionTo(transition);
    }

}
