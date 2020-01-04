/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing.supplier;

import org.rinka.seele.server.engine.resourcing.Selector;
import org.rinka.seele.server.engine.resourcing.Workitem;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Class : Supplier
 * Usage : Base supplier for all implemented suppliers.
 *         Supplier is used to remove participants in candidate set who cannot map the filter conditions.
 *         And produces a candidate set to offer workitem.
 */
public abstract class Supplier extends Selector {

    /**
     * Perform supply action on the candidate set to select a set for offering.
     *
     * @param candidateSet candidate participant set
     * @param workitem     resourcing workitem
     * @return filtered participant set
     */
    public abstract HashSet<ParticipantContext> performSupply(Set<ParticipantContext> candidateSet, Workitem workitem);
}
