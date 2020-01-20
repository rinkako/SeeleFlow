/*
 * Author : Rinka
 * Date   : 2020/1/19
 */
package org.rinka.seele.server.engine.resourcing.participant.agent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class : MetadataPackage
 * Usage :
 */
@NoArgsConstructor
public class MetadataPackage {

    @Getter
    @Setter
    private String namespace;

    @Getter
    @Setter
    private String participantId;

    @Getter
    @Setter
    private String displayName;

    @Getter
    @Setter
    private String reentrantTypeName;

    @Getter
    @Setter
    private String communicationTypeName = "WebSocket";

    @Getter
    private Set<String> skills = new HashSet<>();

    @Getter
    @Setter
    private Map<String, Object> payload;

    public void addSkill(String skill) {
        this.skills.add(skill);
    }

    public void addSkills(Collection<String> skills) {
        this.skills.addAll(skills);
    }
}
