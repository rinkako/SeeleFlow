/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.yurily.seele.server.engine.resourcing.participant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.yurily.seele.server.engine.resourcing.context.RSContext;
import org.yurily.seele.server.engine.resourcing.queue.WorkQueueContainer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class : ParticipantContext
 * Usage :
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParticipantContext extends RSContext {

    public static final String DESC_NAME = "name";
    public static final String DESC_SKILL = "skill";
    public static final String DESC_REST_ENTRY = "rest_entry";
    public static final String DESC_REST_URI = "rest_uri";

    @JsonIgnore
    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private AtomicInteger handlingWorkitemCount = new AtomicInteger(0);

    @JsonIgnore
    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private AtomicInteger handledWorkitemCount = new AtomicInteger(0);

    @JsonIgnore
    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private WorkQueueContainer queueContainer;

    private String namespace;

    /**
     * Participant global id.
     */
    private String participantId;

    /**
     * User-friendly resource name.
     */
    private String displayName;

    /**
     * What skills does this participant has.
     */
    private Set<String> skill = new HashSet<>();

    /**
     * Type of the resource.
     */
    private ParticipantType type;

    /**
     * Type of communication protocol.
     */
    private ParticipantCommunicationType communicationType;

    /**
     * A descriptor guides where RS to find this resource.
     * Agent - usually a hostname with port
     * Human - maybe a string describe his position in organization
     * SubProcess - process global id
     */
    private String entry;

    /**
     * An optional descriptor for `entry` field.
     */
    private String uri;

    /**
     * Is this resource able to handle reentrant workitem.
     */
    private ParticipantReentrantType reentrantType;

    /**
     * Last heartbeat package from participant
     */
    private Object lastBeat;

    /**
     * Last heartbeat package server ts
     */
    private Long lastBeatTimestamp;

    /**
     * Add a collection of skill to this participant.
     *
     * @param skills a List contains skill descriptors
     */
    public void addSkills(Collection<String> skills) {
        this.skill.addAll(skills);
    }

    public void addSkill(String skill) {
        this.skill.add(skill);
    }

    public ParticipantContext(String namespace, String participantId) {
        this.namespace = namespace;
        this.participantId = participantId;
        this.queueContainer = new WorkQueueContainer(this.namespace, this.participantId);
    }
}
