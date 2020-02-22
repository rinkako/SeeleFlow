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
 * Usage : Tracker will perform workitem state transition in a valid transition path,
 *         and will handle the unordered arrival of participant transition requests.
 *         All workitem state transition should be apply here.
 */
@Slf4j
@ToString
public class WorkitemTransitionTracker {

    @Getter
    private WorkitemContext workitem;

    @Getter
    private int appliedEpochWatermark = -1;

    @Getter
    private LinkedList<WorkitemTransition> incomingEpoch = new LinkedList<>();

    @Getter
    private LinkedList<WorkitemTransition> epochTransitions = new LinkedList<>();

    /**
     * Request tracker to perform a transition which guides the workitem to transition from a state to
     * another state if the transition is valid.
     * <p>
     * Transition may not be performed immediately since it may be a unordered arrival from participant
     * workitem transition request and will be put into a graph waiting for its predecessor transitions.
     * <p>
     * After all predecessor transitions successfully performed, this transition will be performed, and
     * update the Epoch Watermark to its epoch Id. Notice that if a transition submitted with an epoch
     * lower than watermark or equals, it will be ignored since it is considered to be a duplicated
     * request from participants.
     * <p>
     * Notice that supervisor transition requests will be executed immediately without queuing, especially
     * the transition with a `FORCE_COMPLETED` target will be performed anyway even a invalid transition
     * unless the workitem is already at final state.
     * <p>
     * In the case of the workitem at final state, any transition will always get a `FinalStateReject`
     * return and nothing is performed.
     *
     * @param transition Transition to perform on binding workitem
     */
    public synchronized TransitionRequestResult transitionTo(WorkitemTransition transition) throws Exception {
        if (this.workitem.isFinalState()) {
            return TransitionRequestResult.FinalStateReject;
        }
        if (transition.getCallerType() == TransitionCallerType.Supervisor) {
            boolean sFlag = false;
            switch (transition.getTarget()) {
                case FORCE_COMPLETED:
                    sFlag = this.microStepTransition(transition, true);
                    break;
                case CANCELLED:
                case ALLOCATED:
                    sFlag = this.microStepTransition(transition, false);
                    break;
            }
            return sFlag ? TransitionRequestResult.Executed : TransitionRequestResult.Invalid;
        } else {
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
            this.marcoStepTransition();
            return this.appliedEpochWatermark >= transition.getEpochId() ?
                    TransitionRequestResult.Executed : TransitionRequestResult.Submitted;
        }
    }

    /**
     * Perform a marco-step transition on binding workitem.
     * <p>
     * Macro-step means the workitem state will go ahead state transitions in the queue
     * until queue is empty or workitem is at final state.
     */
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
            boolean successFlag = this.microStepTransition(transition, false);
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

    /**
     * Perform a micro-step transition on binding workitem.
     * <p>
     * Micro-step means the workitem state will go ahead 1 state according to the
     * transition descriptor if the transition is valid or `forced`. After context
     * state cache changed, the entity will be flushed to the steady memory.
     */
    private synchronized boolean microStepTransition(WorkitemTransition transition, boolean forceTransition) throws Exception {
        if (WorkitemTransition.isTransitionable(this.workitem, transition) || forceTransition) {
            transition.onPrepareExecute(this);
            this.workitem.setState(transition.getTarget());
            this.workitem.flushSteady();
            return true;
        } else {
            log.warn("transition cannot perform, is unordered transition arrival waiting for predecessor?");
            return false;
        }
    }

    /**
     * Create a tracker to tracking workitem state transition.
     *
     * @param workitem binding workitem
     */
    WorkitemTransitionTracker(WorkitemContext workitem) {
        this.workitem = workitem;
    }
}
