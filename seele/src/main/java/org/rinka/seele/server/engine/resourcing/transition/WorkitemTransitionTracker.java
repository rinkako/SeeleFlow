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

import java.util.Comparator;
import java.util.LinkedList;

/**
 * Class : WorkitemTransitionTracker
 * Usage :
 */
@Slf4j
@ToString
public class WorkitemTransitionTracker {

    @Getter
    private WorkitemContext workitem;

    @Getter
    private int currentEpochWatermark = -1;

    @Getter
    private int appliedEpochWatermark = -1;

    @Getter
    private LinkedList<WorkitemTransition> incomingEpoch = new LinkedList<>();

    private LinkedList<WorkitemTransition> epochTransitions = new LinkedList<>();

    public synchronized TransitionRequestResult transitionTo(WorkitemTransition transition) throws Exception {
        this.incomingEpoch.addLast(transition);
        if (!WorkitemTransition.isTransitionValid(transition)) {
            transition.onFailed(this);
            return TransitionRequestResult.Invalid;
        }
        if (transition.getEpochId() <= this.appliedEpochWatermark) {
            log.info("duplicated committed epoch found, ignored: " + transition.toString());
            transition.onIgnored(this);
            return TransitionRequestResult.Duplicated;
        }
        this.epochTransitions.addLast(transition);
        this.epochTransitions.sort(Comparator.comparingInt(WorkitemTransition::getEpochId));
        this.currentEpochWatermark = Math.max(this.currentEpochWatermark, transition.getEpochId());
        this.marcoStepTransition();
        return this.appliedEpochWatermark >= transition.getEpochId() ?
                TransitionRequestResult.Executed : TransitionRequestResult.Submitted;
    }

    private synchronized void marcoStepTransition() throws Exception {
        while (!this.epochTransitions.isEmpty()) {
            WorkitemTransition transition = this.epochTransitions.getFirst();
            if (transition.getEpochId() <= this.appliedEpochWatermark) {
                transition = this.epochTransitions.removeFirst();
                transition.setFinished(true);
                transition.setIgnored(true);
                log.info("duplicated committed epoch found, ignored: " + transition.toString());
                continue;
            }
            boolean successFlag = this.microStepTransition(transition);
            if (!successFlag) {
                transition.onFailed(this);
                break;
            }
            transition = this.epochTransitions.removeFirst();
            transition.setFinished(true);
            transition.onExecuted(this);
            this.appliedEpochWatermark = transition.getEpochId();
        }
    }

    private synchronized boolean microStepTransition(WorkitemTransition transition) throws Exception {
        if (WorkitemTransition.isTransitionable(this.workitem, transition)) {
            this.workitem.setState(transition.getTarget());
            this.workitem.flushSteady();
            return true;
        } else {
            log.warn("unordered transition arrival, waiting for predecessor");
            return false;
        }
    }

    WorkitemTransitionTracker(WorkitemContext workitem) {
        this.workitem = workitem;
    }
}
