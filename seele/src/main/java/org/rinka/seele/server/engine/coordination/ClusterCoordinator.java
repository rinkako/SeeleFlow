/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/7
 */
package org.rinka.seele.server.engine.coordination;

/**
 * Class : ClusterCoordinator
 * Usage :
 */
public interface ClusterCoordinator {

    void addSelfToCluster();

    void removeSelfFromCluster();

    void evictRequests();
}
