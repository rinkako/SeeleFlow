/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing.queue;

import lombok.Data;
import org.rinka.seele.server.engine.resourcing.Workitem;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : WorkQueue
 * Usage : WorkQueue context is an encapsulation of Workitem Queue in a
 * convenient way for resourcing.
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

    /**
     * Workitem in this queue, map in pattern (workitemId, workitemObject)
     */
    private ConcurrentHashMap<String, Workitem> workitems = new ConcurrentHashMap<>();

    /**
     * Add or update a workitem to this queue.
     *
     * @param workitem workitem entity.
     */
    public void addOrUpdate(Workitem workitem) {
        // TODO
    }

    /**
     * Add or update all item in the queue passed by another queue.
     * Entries in another queue will be copied and append to this queue.
     *
     * @param queue queue to be added
     */
    public void addFromQueue(WorkQueue queue) {
        // TODO
    }

    /**
     * Remove a workitem from this queue.
     *
     * @param workitem workitem context
     */
    public void Remove(Workitem workitem) {
        // TODO
    }

    /**
     * Removes all entries in a queue.
     *
     * @param queue the queue of items to remove
     */
    public void RemoveFromQueue(WorkQueue queue) {
        // TODO
    }

    /**
     * Remove all entries from a specific process runtime.
     *
     * @param rtid process rtid
     */
    public void RemoveByRTID(String rtid) {
        // TODO
    }

    /**
     * Check this queue is empty.
     *
     * @return true if queue empty
     */
    public boolean isEmpty() {
        // TODO
        return false;
    }

    /**
     * Count the queue length.
     *
     * @return number of workitems in this queue
     */
    public int count() {
        // TODO
        return 0;
    }

    /**
     * Clear the queue, remove all entries.
     */
    public void clear() {
        // TODO
    }

    /**
     * Get a concurrent hash map of all workitem in queue.
     *
     * @return all members of the queue as a HashMap of (workitemId, workitemObject)
     */
    public Map<String, Workitem> asMap() {
        // TODO
        return null;
    }

    /**
     * Get a copied hash set of all workitem in queue.
     *
     * @return all members of the queue as a HashSet
     */
    public Set<Workitem> asSet() {
        // TODO
        return null;
    }

    /**
     * Remove a workitem from all queue.
     *
     * @param workitem workitem context
     */
    public static void removeFromAllWorkQueue(Workitem workitem) {
        // TODO
    }

    /**
     * Get the specific queue context and store to steady.
     *
     * @param ownerWorkerId queue owner worker id
     * @param queueType     queue type enum
     * @param forceReload   force reload from steady and refresh cache
     * @return a workqueue context
     */
    public static WorkQueue of(String ownerWorkerId, WorkQueueType queueType, boolean forceReload) {
        // TODO
        return null;
    }

    /**
     * Write resource event log to steady.
     *
     * @param workitem workitem context
     */
    private void LogEvent(Workitem workitem) {
        // TODO
    }

    /**
     * Refresh work queue from steady.
     * NOTICE this method will be called when perform GET DATA type of queue context
     * to make sure data consistency among all RS.
     */
    private static void syncSteady() {
        // TODO
    }

    /**
     * Create a new work queue context.
     * NOTICE that usually this method should not be called unless <b>worklisted</b> queue.
     *
     * @param id            work queue global id
     * @param ownerWorkerId owner worker global id
     * @param type          queue type enum
     */
    public WorkQueue(String id, String ownerWorkerId, WorkQueueType type) {
        this.queueId = id;
        this.ownerWorkerId = ownerWorkerId;
        this.type = type;
        // here no need for queued workitem refresh, any GET methods will refresh automatically.
    }
}
