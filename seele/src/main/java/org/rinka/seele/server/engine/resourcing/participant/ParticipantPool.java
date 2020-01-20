/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.rinka.seele.server.engine.resourcing.participant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.participant.agent.MetadataPackage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : ParticipantPool
 * Usage :
 */
@Slf4j
public class ParticipantPool {

    private static ConcurrentHashMap<String, NamespacedParticipantPool> namespacedPool = new ConcurrentHashMap<>();

    public static NamespacedParticipantPool namespace(String namespace) {
        return ParticipantPool.namespacedPool
                .computeIfAbsent(namespace, s -> {
                    log.info("create new pool for namespace: " + namespace);
                    return new NamespacedParticipantPool(namespace);
                });
    }

    public static class NamespacedParticipantPool {

        public NamespacedParticipantPool(String namespace) {
            this.namespace = namespace;
        }

        private final String namespace;

        private final ConcurrentHashMap<String, ParticipantContext> namespacedPool = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, HashSet<ParticipantContext>> skilledPool = new ConcurrentHashMap<>();

        public void addAgentParticipant(String participantId, MetadataPackage descriptor) {
            ParticipantContext pc = new ParticipantContext();
            pc.setParticipantId(participantId);
            pc.setDisplayName(descriptor.getDisplayName());
            pc.setCommunicationType(Enum.valueOf(ParticipantCommunicationType.class, descriptor.getCommunicationTypeName()));
            pc.setReentrantType(Enum.valueOf(ParticipantReentrantType.class, descriptor.getReentrantTypeName()));
//        if (pc.getCommunicationType() == ParticipantCommunicationType.RestPost) {
//            pc.setEntry(descriptor.getOrDefault(ParticipantContext.DESC_REST_ENTRY, ""));
//            pc.setUri(descriptor.getOrDefault(ParticipantContext.DESC_REST_URI, ""));
//        }
            Set<String> skills =descriptor.getSkills();
            for (String skill : skills) {
                pc.addSkill(skill);
                HashSet<ParticipantContext> skillCtx = skilledPool.computeIfAbsent(skill, s -> new HashSet<>());
                skillCtx.add(pc);
            }
            this.namespacedPool.put(participantId, pc);
        }

        public ParticipantContext removeAgentParticipant(String participantId) {
            return this.namespacedPool.remove(participantId);
        }

        public Set<ParticipantContext> getSkilledParticipants(String... skills) {
            Set<ParticipantContext> result = new HashSet<>();
            for (String skill : skills) {
                result.addAll(this.skilledPool.get(skill));
            }
            return result;
        }
    }
}
