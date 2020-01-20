/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.connect.rest;


import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : SupervisorRestPool
 * Usage :
 */
public class SupervisorRestPool {

    private static ConcurrentHashMap<String, SupervisorGroup> pool = new ConcurrentHashMap<>();

    public static SupervisorGroup namespace(String namespace) {
        return SupervisorRestPool.pool.computeIfAbsent(namespace, s -> new SupervisorGroup(namespace));
    }

    public static Optional<SupervisorGroup> remove(String namespace) {
        return Optional.ofNullable(SupervisorRestPool.pool.remove(namespace));
    }
}
