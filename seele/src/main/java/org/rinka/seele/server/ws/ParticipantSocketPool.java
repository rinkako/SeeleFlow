/*
 * Author : Rinka
 * Date   : 2020/1/16
 * Contact: gzlinjia@corp.netease.com
 */
package org.rinka.seele.server.ws;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : ParticipantSocketPool
 * Usage :
 */
@Slf4j
public class ParticipantSocketPool {
    private static ConcurrentHashMap<String, SocketIOClient> pool = new ConcurrentHashMap<>();

    public static void add(String participantId, SocketIOClient client) {
        ParticipantSocketPool.pool.put(participantId, client);
        log.info("client put into participant pool: " + client.getSessionId());
    }

    public static SocketIOClient remove(String participantId) {
        log.info("client remove from participant pool: " + participantId);
        return ParticipantSocketPool.pool.remove(participantId);
    }

    public static void clear() {
        ParticipantSocketPool.pool.clear();
    }

    public static boolean contains(String participantId) {
        return ParticipantSocketPool.pool.containsKey(participantId);
    }
}
