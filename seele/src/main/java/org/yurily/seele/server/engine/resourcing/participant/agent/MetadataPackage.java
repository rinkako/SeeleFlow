/*
 * Author : Rinka
 * Date   : 2020/1/19
 */
package org.yurily.seele.server.engine.resourcing.participant.agent;

import lombok.*;
import org.yurily.seele.server.util.JsonUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class : MetadataPackage
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
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

    public MetadataPackage(String descriptor) throws Exception {
        Map<String, Object> mp = JsonUtil.parse(descriptor, Map.class);
        Class clazz = this.getClass();
        for (Map.Entry<String, Object> kvp : mp.entrySet()) {
            Field f = clazz.getDeclaredField(kvp.getKey());
            if (Set.class.isAssignableFrom(f.getType())) {
                f.set(this, new HashSet<String>((Collection<? extends String>) kvp.getValue()));
            } else {
                f.set(this, kvp.getValue());
            }
        }
    }

    public void addSkill(String skill) {
        this.skills.add(skill);
    }

    public void addSkills(Collection<String> skills) {
        this.skills.addAll(skills);
    }
}
