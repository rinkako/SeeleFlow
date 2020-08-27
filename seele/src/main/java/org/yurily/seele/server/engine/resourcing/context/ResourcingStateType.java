/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.yurily.seele.server.engine.resourcing.context;

public enum ResourcingStateType {
    /**
     * Arrive at Seele, but never handle
     */
    CREATED,

    /**
     * Resourcing finished, notified worker for handle
     */
    ALLOCATED,

    /**
     * Resourcing failed
     */
    BAD_ALLOCATED,

    /**
     * Workitem already cancelled
     */
    CANCELLED,

    /**
     * Accepted by participant, but not handle yet
     */
    ACCEPTED,

    /**
     * Participant already fired the workitem, waiting for complete
     */
    RUNNING,

    /**
     * Workitem finished with any exception
     */
    EXCEPTION,

    /**
     * Workitem was forced to be complete by supervisor request
     */
    FORCE_COMPLETED,

    /**
     * Workitem completed normally
     */
    COMPLETED,

    /**
     * Any state above
     */
    ANY
}
