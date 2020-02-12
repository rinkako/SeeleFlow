/*
 * Author : Rinka
 * Date   : 2020/2/10
 */
package org.rinka.seele.server.engine.resourcing.queue;

import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.Workitem;

import java.util.Set;

/**
 * Class : WorkQueueContainer
 * Usage :
 */
@Slf4j
public class WorkQueueContainer {

    /**
     * Participant offered workitem queue.
     */
    private WorkQueue offeredQueue;

    /**
     * Participant allocated workitem queue.
     */
    private WorkQueue allocatedQueue;

    /**
     * Participant started workitem queue.
     */
    private WorkQueue startedQueue;

    /**
     * Participant suspended workitem queue.
     */
    private WorkQueue suspendedQueue;

    /**
     * Queue container owner worker global id.
     */
    private String ownerWorkerId;

    /**
     * Namespace of container
     */
    private String namespace;

    /**
     * Type of Queue container owner.
     */
    private WorkQueueContainerType type;

    /**
     * Move workitem queue: OFFERED -> ALLOCATED
     *
     * @param workitem workitem context
     */
    public void moveOfferedToAllocated(Workitem workitem) {
        this.move(workitem, WorkQueueType.OFFERED, WorkQueueType.ALLOCATED);
    }

    /**
     * Move workitem queue: ALLOCATED -> OFFERED
     *
     * @param workitem workitem context
     */
    public void moveAllocatedToOffered(Workitem workitem) {
        this.move(workitem, WorkQueueType.ALLOCATED, WorkQueueType.OFFERED);
    }

    /**
     * Move workitem queue: OFFERED -> STARTED
     *
     * @param workitem workitem context
     */
    public void moveOfferedToStarted(Workitem workitem) {
        this.move(workitem, WorkQueueType.OFFERED, WorkQueueType.STARTED);
    }

    /**
     * Move workitem queue: OFFERED -> STARTED
     *
     * @param workitem workitem context
     */
    public void moveStartedToOffered(Workitem workitem) {
        this.move(workitem, WorkQueueType.STARTED, WorkQueueType.OFFERED);
    }

    /**
     * Move workitem queue: ALLOCATED -> STARTED
     *
     * @param workitem workitem context
     */
    public void moveAllocatedToStarted(Workitem workitem) {
        this.move(workitem, WorkQueueType.ALLOCATED, WorkQueueType.STARTED);
    }

    /**
     * Move workitem queue: STARTED -> ALLOCATED
     *
     * @param workitem workitem context
     */
    public void moveStartedToAllocated(Workitem workitem) {
        this.move(workitem, WorkQueueType.STARTED, WorkQueueType.ALLOCATED);
    }

    /**
     * Move workitem queue: STARTED -> SUSPENDED
     *
     * @param workitem workitem context
     */
    public void moveStartedToSuspend(Workitem workitem) {
        this.move(workitem, WorkQueueType.STARTED, WorkQueueType.SUSPENDED);
    }

    /**
     * Move workitem queue: SUSPENDED -> STARTED
     *
     * @param workitem workitem context
     */
    public void moveSuspendToStarted(Workitem workitem) {
        this.move(workitem, WorkQueueType.SUSPENDED, WorkQueueType.STARTED);
    }

    /**
     * Move a workitem from a queue to another queue.
     * NOTICE that this method usually should NOT be called outside, since not all move is valid.
     *
     * @param workitem workitem context to be moved
     * @param from     from queue type
     * @param to       to queue type
     */
    public void move(Workitem workitem, WorkQueueType from, WorkQueueType to) {
        this.removeFromQueue(workitem, from);
        this.addToQueue(workitem, to);
    }

    /**
     * Add or update a workitem to a queue.
     *
     * @param workitem workitem context
     * @param type     queue type
     */
    public void addToQueue(Workitem workitem, WorkQueueType type) {
        WorkQueue wq = this.getQueue(type);
        wq.addOrUpdate(workitem);
    }

    /**
     * Add or update workitems to a queue.
     *
     * @param addQueue workitem context queue to add
     * @param type     queue type
     */
    public void addToQueue(WorkQueue addQueue, WorkQueueType type) {
        WorkQueue wq = this.getQueue(type);
        wq.addFromQueue(addQueue);
    }

    /**
     * Remove a workitem from a queue.
     *
     * @param workitem workitem context
     * @param type     queue type
     */
    public void removeFromQueue(Workitem workitem, WorkQueueType type) {
        this.getQueue(type).remove(workitem);
    }

    /**
     * Remove workitems from a queue.
     *
     * @param removeQueue workitem context queue to remove
     * @param type        queue type
     */
    public void removeFromQueue(WorkQueue removeQueue, WorkQueueType type) {
        this.getQueue(type).removeFromQueue(removeQueue);
    }

    /**
     * Remove workitems from a queue belong to a specific rtid.
     *
     * @param rtid process rtid.
     * @param type queue type
     */
    public void removeFromQueueByRTID(String rtid, WorkQueueType type) {
        this.getQueue(type).removeByRTID(rtid);
    }

    /**
     * Remove workitems from all queues belong to a specific rtid.
     *
     * @param rtid process rtid.
     */
    public void RemoveFromAllQueueByRTID(String rtid) {
        if (this.type == WorkQueueContainerType.AdminSet) {
            this.removeFromQueueByRTID(rtid, WorkQueueType.UNOFFERED);
            this.removeFromQueueByRTID(rtid, WorkQueueType.WORKLISTED);
        } else {
            this.removeFromQueueByRTID(rtid, WorkQueueType.OFFERED);
            this.removeFromQueueByRTID(rtid, WorkQueueType.ALLOCATED);
            this.removeFromQueueByRTID(rtid, WorkQueueType.STARTED);
            this.removeFromQueueByRTID(rtid, WorkQueueType.SUSPENDED);
        }
    }

    /**
     * Get all workitem in a queue.
     *
     * @param type queue type
     * @return workitem hash set.
     */
    public Set<Workitem> getQueuedWorkitem(WorkQueueType type) {
        return this.getQueue(type).copyToSet();
    }

    /**
     * Check if a queue contains a workitem.
     *
     * @param workitemId workitem global id
     * @param type       queue type
     * @return true if contains
     */
    public boolean contains(String workitemId, WorkQueueType type) {
        return this.getQueue(type).contains(workitemId);
    }

    /**
     * Check if any queue contains a workitem.
     *
     * @param workitemId workitem global id
     * @return true if contains
     */
    public boolean containsAny(String workitemId) {
        for (int qType = WorkQueueType.OFFERED.ordinal(); qType <= WorkQueueType.SUSPENDED.ordinal(); qType++) {
            if (this.contains(workitemId, WorkQueueType.values()[qType])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a workitem from a queue by its global id.
     *
     * @param workitemId workitem global id
     * @param type       queue type
     * @return workitem context
     */
    public Workitem get(String workitemId, WorkQueueType type) {
        return this.getQueue(type).get(workitemId);
    }

    /**
     * Check if a queue is null.
     *
     * @param type queue type
     * @return true if queue is null
     */
    public boolean isNullQueue(WorkQueueType type) {
        return this.directlyGetQueue(type) == null;
    }

    /**
     * Check if a queue is null or empty.
     *
     * @param type queue type
     * @return true if queue is null or empty
     */
    public boolean isNullOrEmptyQueue(WorkQueueType type) {
        WorkQueue wq = this.directlyGetQueue(type);
        return wq == null || wq.isEmpty();
    }

    /**
     * Directly get the queue reference in the container.
     *
     * @param type queue type
     * @return queue reference.
     */
    public WorkQueue directlyGetQueue(WorkQueueType type) {
        switch (type) {
            case OFFERED:
                return this.offeredQueue;
            case ALLOCATED:
                return this.allocatedQueue;
            case STARTED:
                return this.startedQueue;
            case SUSPENDED:
                return this.suspendedQueue;
        }
        return null;
    }

    /**
     * Get the queue reference in the container, if a queue is null then
     * it will be generated by using {@code WorkQueue.getContext}.
     *
     * @param type queue type
     * @return queue reference
     */
    public WorkQueue getQueue(WorkQueueType type) {
        switch (type) {
            case OFFERED:
                if (this.offeredQueue == null) {
                    this.offeredQueue = WorkQueue.of(this.namespace, this.ownerWorkerId, type);
                }
                return this.offeredQueue;
            case ALLOCATED:
                if (this.allocatedQueue == null) {
                    this.allocatedQueue = WorkQueue.of(this.namespace, this.ownerWorkerId, type);
                }
                return this.allocatedQueue;
            case STARTED:
                if (this.startedQueue == null) {
                    this.startedQueue = WorkQueue.of(this.namespace, this.ownerWorkerId, type);
                }
                return this.startedQueue;
            case SUSPENDED:
                if (this.suspendedQueue == null) {
                    this.suspendedQueue = WorkQueue.of(this.namespace, this.ownerWorkerId, type);
                }
                return this.suspendedQueue;
        }
        return null;
    }

    /**
     * Create a new work queue container.
     * Private constructor for prevent creating new instance outside.
     *
     * @param participantId owner wparticipant id
     * @param type          container type
     */
    public WorkQueueContainer(String namespace, String participantId, WorkQueueContainerType type) {
        this.namespace = namespace;
        this.ownerWorkerId = participantId;
        this.type = type;
    }

    public WorkQueueContainer(String namespace, String participantId) {
        this(namespace, participantId, WorkQueueContainerType.ParticipantSet);
    }

    /**
     * Get container owner worker global id.
     *
     * @return worker gid string
     */
    public String getWorkerGid() {
        return this.ownerWorkerId;
    }

    /**
     * Get container type.
     *
     * @return container type enum
     */
    public WorkQueueContainerType getType() {
        return this.type;
    }

    public enum WorkQueueContainerType {
        /**
         * Container for external participant
         */
        ParticipantSet,

        /**
         * Container for namespace admin
         */
        AdminSet
    }

}
