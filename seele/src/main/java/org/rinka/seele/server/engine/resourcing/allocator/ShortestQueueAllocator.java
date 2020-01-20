/*
 * Author : Rinka
 * Date   : 2020/1/19
 */
package org.rinka.seele.server.engine.resourcing.allocator;

import org.rinka.seele.server.engine.resourcing.Workitem;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;

import java.util.Set;

/**
 * Class : ShortestQueueAllocator
 * Usage :
 */
public class ShortestQueueAllocator extends Allocator {

    /**
     * Perform allocation on the candidate set to select one resource to handle a workitem.
     *
     * @param candidateSet candidate participant set
     * @param workitem     workitem context
     * @return selected participant
     */
    @Override
    public ParticipantContext performAllocate(Set<ParticipantContext> candidateSet, Workitem workitem) {
        if (candidateSet.size() == 0) {
            return null;
        }
        int currentShortest = Integer.MAX_VALUE;
        int currentLength;
        ParticipantContext chosenOne = candidateSet.iterator().next();
        for (ParticipantContext p : candidateSet) {
            currentLength = p.getHandlingWorkitemCount().get();
            if (currentLength == 0) {
                return p;
            }
            if (currentLength < currentShortest) {
                currentShortest = currentLength;
                chosenOne = p;
            }
        }
        return chosenOne;
    }
}
