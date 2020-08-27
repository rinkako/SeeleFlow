/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.yurily.seele.server.engine.resourcing.participant;

import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.engine.resourcing.participant.agent.MetadataPackage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : ParticipantPool
 * Usage :
 */
@Slf4j
public class ParticipantPool {

    private static ConcurrentHashMap<String, NamespacedParticipantPool> namespacingPool = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, ParticipantContext> sessionIdPool = new ConcurrentHashMap<>();

    public static NamespacedParticipantPool namespace(String namespace) {
        return ParticipantPool.namespacingPool
                .computeIfAbsent(namespace, s -> {
                    log.info("create new pool for namespace: " + namespace);
                    return new NamespacedParticipantPool(namespace);
                });
    }

    public static ParticipantContext getParticipantBySessionId(String sessionId) {
        return ParticipantPool.sessionIdPool.get(sessionId);
    }

    public static ParticipantContext removeParticipantBySessionId(String sessionId) {
        ParticipantContext pc = ParticipantPool.sessionIdPool.remove(sessionId);
        return ParticipantPool.namespacingPool.get(pc.getNamespace()).removeAgentParticipant(pc);
    }

    public static class NamespacedParticipantPool {

        public NamespacedParticipantPool(String namespace) {
            this.namespace = namespace;
        }

        private final String namespace;

        private final ConcurrentHashMap<String, ParticipantContext> namespacedPool = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, HashSet<ParticipantContext>> skilledPool = new ConcurrentHashMap<>();

        public void addAgentParticipant(String sessionId, String participantId, MetadataPackage descriptor) {
            ParticipantContext pc = new ParticipantContext(this.namespace, participantId);
            pc.setDisplayName(descriptor.getDisplayName());
            pc.setCommunicationType(Enum.valueOf(ParticipantCommunicationType.class, descriptor.getCommunicationTypeName()));
            pc.setReentrantType(Enum.valueOf(ParticipantReentrantType.class, descriptor.getReentrantTypeName()));
//        if (pc.getCommunicationType() == ParticipantCommunicationType.RestPost) {
//            pc.setEntry(descriptor.getOrDefault(ParticipantContext.DESC_REST_ENTRY, ""));
//            pc.setUri(descriptor.getOrDefault(ParticipantContext.DESC_REST_URI, ""));
//        }
            Set<String> skills = descriptor.getSkills();
            for (String skill : skills) {
                pc.addSkill(skill);
                HashSet<ParticipantContext> skillCtx = skilledPool.computeIfAbsent(skill, s -> new HashSet<>());
                skillCtx.add(pc);
            }
            this.namespacedPool.put(participantId, pc);
            ParticipantPool.sessionIdPool.put(sessionId, pc);
        }

        private ParticipantContext removeAgentParticipant(ParticipantContext participantContext) {
            if (participantContext == null) {
                return null;
            }
            return this.namespacedPool.remove(participantContext.getParticipantId());
        }

        public ParticipantContext getParticipant(String participantId) {
            return this.namespacedPool.get(participantId);
        }

        public Set<ParticipantContext> getParticipants() {
            return new HashSet<>(this.namespacedPool.values());
        }

        public Set<ParticipantContext> getSkilledParticipants(String... skills) {
            Set<ParticipantContext> result = new HashSet<>();
            for (String skill : skills) {
                HashSet<ParticipantContext> skilled = this.skilledPool.get(skill);
                if (skilled != null) {
                    result.addAll(skilled);
                }
            }
            return result;
        }
    }
}
