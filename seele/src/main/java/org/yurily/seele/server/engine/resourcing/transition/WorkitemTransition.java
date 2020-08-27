/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.yurily.seele.server.engine.resourcing.transition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.yurily.seele.server.engine.resourcing.context.ResourcingStateType;
import org.yurily.seele.server.engine.resourcing.context.WorkitemContext;

import java.io.Serializable;
import java.util.*;

/**
 * Class : WorkitemTransition
 * Usage : Workitem state transition action descriptor
 */
@ToString(exclude = {"callback"})
@EqualsAndHashCode
public class WorkitemTransition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private int epochId;

    @Getter
    private ResourcingStateType from;

    @Getter
    private ResourcingStateType target;

    @Getter
    private TransitionCallerType callerType;

    @Getter
    @Setter
    private boolean finished = false;

    @Getter
    @Setter
    private boolean ignored = false;

    @Getter
    @Setter
    @JsonIgnore
    private BaseTransitionCallback callback;

    public WorkitemTransition(TransitionCallerType callerType,
                              ResourcingStateType from,
                              ResourcingStateType target,
                              int epochId) {
        this(callerType, from, target, epochId, null);
    }

    public WorkitemTransition(TransitionCallerType callerType,
                              ResourcingStateType from,
                              ResourcingStateType target,
                              int epochId,
                              BaseTransitionCallback callback) {
        this.callerType = callerType;
        this.from = from;
        this.target = target;
        this.epochId = epochId;
        this.callback = callback;
    }

    void onPrepareExecute(WorkitemTransitionTracker transitionTracker) throws Exception {
        if (this.callback != null) {
            this.callback.onPrepareExecute(transitionTracker, this);
        }
    }

    void onExecuted(WorkitemTransitionTracker transitionTracker) throws Exception {
        if (this.callback != null) {
            this.callback.onExecuted(transitionTracker, this);
        }
    }

    void onFailed(WorkitemTransitionTracker transitionTracker) throws Exception {
        if (this.callback != null) {
            this.callback.onFailed(transitionTracker, this);
        }
    }

    void onIgnored(WorkitemTransitionTracker transitionTracker) throws Exception {
        if (this.callback != null) {
            this.callback.onIgnored(transitionTracker, this);
        }
    }

    public static boolean isTransitionable(WorkitemContext workitem, WorkitemTransition transition) {
        if (transition.getFrom() == ResourcingStateType.ANY) {
            return WorkitemTransition.isTransitionValid(workitem.getState(), transition.target);
        } else {
            return workitem.getState().equals(transition.getFrom());
        }
    }

    public static boolean isTransitionValid(WorkitemTransition transition) {
        return WorkitemTransition.isTransitionValid(transition.from, transition.target);
    }

    public static boolean isTransitionValid(ResourcingStateType from, ResourcingStateType target) {
        return WorkitemTransition.transitionableGraph.get(from).contains(target);
    }

    private static final Map<ResourcingStateType, Set<ResourcingStateType>> transitionableGraph = new HashMap<>();

    static {
        // CREATED
        Set<ResourcingStateType> createSet = new HashSet<>();
        createSet.add(ResourcingStateType.ALLOCATED);
        createSet.add(ResourcingStateType.BAD_ALLOCATED);
        createSet.add(ResourcingStateType.CANCELLED);
        transitionableGraph.put(ResourcingStateType.CREATED, createSet);
        // BAD_ALLOCATED
        Set<ResourcingStateType> badAllocateSet = new HashSet<>();
        badAllocateSet.add(ResourcingStateType.ALLOCATED);
        badAllocateSet.add(ResourcingStateType.BAD_ALLOCATED);
        badAllocateSet.add(ResourcingStateType.CANCELLED);
        transitionableGraph.put(ResourcingStateType.BAD_ALLOCATED, badAllocateSet);
        // ALLOCATED
        Set<ResourcingStateType> allocateSet = new HashSet<>();
        allocateSet.add(ResourcingStateType.FORCE_COMPLETED);
        allocateSet.add(ResourcingStateType.ACCEPTED);
        allocateSet.add(ResourcingStateType.BAD_ALLOCATED);
        allocateSet.add(ResourcingStateType.CANCELLED);
        transitionableGraph.put(ResourcingStateType.ALLOCATED, allocateSet);
        // ACCEPTED
        Set<ResourcingStateType> acceptSet = new HashSet<>();
        acceptSet.add(ResourcingStateType.FORCE_COMPLETED);
        acceptSet.add(ResourcingStateType.RUNNING);
        transitionableGraph.put(ResourcingStateType.ACCEPTED, acceptSet);
        // ACCEPTED
        Set<ResourcingStateType> runningSet = new HashSet<>();
        runningSet.add(ResourcingStateType.COMPLETED);
        runningSet.add(ResourcingStateType.EXCEPTION);
        runningSet.add(ResourcingStateType.FORCE_COMPLETED);
        transitionableGraph.put(ResourcingStateType.RUNNING, runningSet);
        // CANCELLED
        transitionableGraph.put(ResourcingStateType.CANCELLED, Collections.EMPTY_SET);
        // COMPLETED
        transitionableGraph.put(ResourcingStateType.COMPLETED, Collections.EMPTY_SET);
        // FORCE_COMPLETED
        transitionableGraph.put(ResourcingStateType.FORCE_COMPLETED, Collections.EMPTY_SET);
        // EXCEPTION
        transitionableGraph.put(ResourcingStateType.EXCEPTION, Collections.EMPTY_SET);
    }
}
