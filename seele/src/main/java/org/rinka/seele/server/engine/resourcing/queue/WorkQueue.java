/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing.queue;

import lombok.Data;

import java.io.Serializable;

/**
 * Class : WorkQueue
 * Usage : WorkQueue context is an encapsulation of Workitem Queue in a
 *         convenient way for resourcing.
 */
@Data
public class WorkQueue implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Queue global id.
     */
    private String queueId;

    /**
     * Queue owner worker id, {@code GlobalContext.WORKQUEUE_ADMIN_PREFIX} if an admin queue.
     */
    private String ownerWorkerId;

    /**
     * Queue type enum.
     */
    private WorkQueueType type;


}
