/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.rinka.seele.server.engine.resourcing.participant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : ParticipantPool
 * Usage :
 */
@Slf4j
public class ParticipantPool {

    private static ConcurrentHashMap<String, ConcurrentHashMap<String, ParticipantContext>> namespacedPool = new ConcurrentHashMap<>();

    public static void addAgentParticipant(String namespace, String participantId, Map<String, String> descriptor) {
        ConcurrentHashMap<String, ParticipantContext> nsPool = ParticipantPool.namespacedPool
                .computeIfAbsent(namespace, s -> {
                    log.info("create new pool for namespace: " + namespace);
                    return new ConcurrentHashMap<>();
                });
        ParticipantContext pc = new ParticipantContext();
        pc.setParticipantId(participantId);
        pc.setDisplayName(descriptor.getOrDefault(ParticipantContext.DESC_NAME, ""));
        pc.setCommunicationType(ParticipantCommunicationType.WebSocket);  // TODO
        pc.setReentrantType(ParticipantReentrantType.NotReentrant);  // TODO
        if (pc.getCommunicationType() == ParticipantCommunicationType.RestPost) {
            pc.setEntry(descriptor.getOrDefault(ParticipantContext.DESC_REST_ENTRY, ""));
            pc.setUri(descriptor.getOrDefault(ParticipantContext.DESC_REST_URI, ""));
        }
        JSONArray skills = JSON.parseArray(descriptor.getOrDefault(ParticipantContext.DESC_SKILL, "[]"));
        pc.addSkills(skills.toJavaList(String.class));
        nsPool.put(participantId, pc);
    }
}
