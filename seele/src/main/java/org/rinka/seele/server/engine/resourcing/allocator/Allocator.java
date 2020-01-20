/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/3
 */
package org.rinka.seele.server.engine.resourcing.allocator;

import org.rinka.seele.server.engine.resourcing.Selector;
import org.rinka.seele.server.engine.resourcing.Workitem;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;

import java.util.Set;

/**
 * Class : Allocator
 * Usage : Base allocator for all implemented allocators.
 * Allocator is used to choose a participant to handle task from candidate set.
 */
public abstract class Allocator extends Selector {

    /**
     * Perform allocation on the candidate set to select one resource to handle a workitem.
     *
     * @param candidateSet candidate participant set
     * @param workitem     workitem context
     * @return selected participant
     */
    public abstract ParticipantContext performAllocate(Set<ParticipantContext> candidateSet, Workitem workitem);
}
