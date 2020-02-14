/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing.queue;

import lombok.Data;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleWorkitemRepository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
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

    private SeeleWorkitemRepository repository;

    /**
     * Namespace
     */
    private String namespace;

    /**
     * Queue global id.
     */
    private String queueId;

    /**
     * Queue owner participant id
     */
    private String ownerParticipantId;

    /**
     * Queue type enum.
     */
    private WorkQueueType type;

    /**
     * Workitem in this queue, map in pattern (workitemId, workitemObject)
     */
    private ConcurrentHashMap<String, WorkitemContext> workitems = new ConcurrentHashMap<>();

    public WorkitemContext get(String wid) {
        return this.workitems.get(wid);
    }

    /**
     * Add or update a workitem to this queue.
     *
     * @param workitem workitem entity.
     */
    public void addOrUpdate(WorkitemContext workitem) throws Exception {
        SeeleWorkitemEntity workitemEntity = workitem.getEntity();
        workitemEntity.setQueueId(this.queueId);
        switch (this.type) {
            case ALLOCATED:
                workitem.setState(WorkitemContext.ResourcingStateType.ALLOCATED);
                break;
            case ACCEPTED:
                workitem.setState(WorkitemContext.ResourcingStateType.ACCEPTED);
                break;
            case STARTED:
                workitem.setState(WorkitemContext.ResourcingStateType.RUNNING);
                break;
        }
        workitem.flushSteady();
        this.workitems.put(workitem.getWid(), workitem);
    }

    /**
     * Add or update all item in the queue passed by another queue.
     * Entries in another queue will be copied and append to this queue.
     *
     * @param queue queue to be added
     */
    public void addFromQueue(WorkQueue queue) throws Exception {
        for (WorkitemContext w : queue.getWorkitems().values()) {
            this.addOrUpdate(w);
        }
    }

    /**
     * Remove a workitem from this queue.
     *
     * @param workitem workitem context
     */
    public WorkitemContext remove(WorkitemContext workitem) {
        return this.workitems.remove(workitem.getWid());
    }

    /**
     * Removes all entries in a queue.
     *
     * @param queue the queue of items to remove
     */
    public void removeFromQueue(WorkQueue queue) {
        for (WorkitemContext w : queue.getWorkitems().values()) {
            this.remove(w);
        }
    }

    /**
     * Remove all entries from a specific process runtime.
     *
     * @param rtid process rtid
     */
    public void removeByRTID(String rtid) {
        // TODO
    }

    /**
     * Check this queue is empty.
     *
     * @return true if queue empty
     */
    public boolean isEmpty() {
        return this.workitems.size() == 0;
    }

    /**
     * Count the queue length.
     *
     * @return number of workitems in this queue
     */
    public int count() {
        return this.workitems.size();
    }

    /**
     * Clear the queue, remove all entries.
     */
    public void clear() {
        this.workitems.clear();
    }

    public boolean contains(String workitemId) {
        return this.workitems.containsKey(workitemId);
    }

    /**
     * Get a concurrent hash map of all workitem in queue.
     *
     * @return all members of the queue as a HashMap of (workitemId, workitemObject)
     */
    public Map<String, WorkitemContext> copyToMap() {
        return new HashMap<>(this.workitems);
    }

    /**
     * Get a copied hash set of all workitem in queue.
     *
     * @return all members of the queue as a HashSet
     */
    public Set<WorkitemContext> copyToSet() {
        return new HashSet<>(this.workitems.values());
    }

    /**
     * Get the specific queue context and store to steady.
     *
     * @param ownerWorkerId queue owner worker id
     * @param queueType     queue type enum
     * @return a workqueue context
     */
    public static WorkQueue of(SeeleWorkitemRepository repository, String namespace, String ownerWorkerId, WorkQueueType queueType) {
        WorkQueue workQueue = new WorkQueue();
        workQueue.repository = repository;
        workQueue.ownerParticipantId = ownerWorkerId;
        workQueue.namespace = namespace;
        workQueue.type = queueType;
        workQueue.queueId = String.format("%s&%s&%s", namespace, ownerWorkerId, queueType.name());
        return workQueue;
    }

    /**
     * Refresh work queue from steady.
     * NOTICE this method will be called when perform GET DATA type of queue context
     * to make sure data consistency among all RS.
     */
    private static void syncSteady() {
        // TODO
    }
}
