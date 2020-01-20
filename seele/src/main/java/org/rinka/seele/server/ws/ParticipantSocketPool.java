/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.rinka.seele.server.ws;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Class : ParticipantSocketPool
 * Usage :
 */
@Slf4j
public class ParticipantSocketPool {
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, SocketIOClient>> pool = new ConcurrentHashMap<>();

    public static void add(String namespace, String participantId, SocketIOClient client) {
        ConcurrentHashMap<String, SocketIOClient> scopedPool = ParticipantSocketPool.pool.computeIfAbsent(namespace, s -> new ConcurrentHashMap<>());
        scopedPool.put(participantId, client);
        log.info("client put into participant pool: " + client.getSessionId());
    }

    public static SocketIOClient remove(String namespace, String participantId) {
        ConcurrentHashMap<String, SocketIOClient> scopedPool = ParticipantSocketPool.pool.computeIfAbsent(namespace, s -> new ConcurrentHashMap<>());
        log.info("client remove from participant pool: " + participantId);
        return scopedPool.remove(participantId);
    }

    public static void clear(String namespace) {
        ParticipantSocketPool.pool.remove(namespace);
    }

    public static void clear() {
        ParticipantSocketPool.pool.clear();
    }

    public static boolean contains(String namespace, String participantId) {
        ConcurrentHashMap<String, SocketIOClient> scopedPool = ParticipantSocketPool.pool.computeIfAbsent(namespace, s -> new ConcurrentHashMap<>());
        return scopedPool.containsKey(participantId);
    }
}
