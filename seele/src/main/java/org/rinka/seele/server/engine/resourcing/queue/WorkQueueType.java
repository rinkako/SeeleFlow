/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing.queue;

/**
 * Enum: WorkQueueType
 */
public enum WorkQueueType {
    UNDEFINED,
    OFFERED,
    ALLOCATED,
    STARTED,
    SUSPENDED,
    UNOFFERED,
    WORKLISTED
}
