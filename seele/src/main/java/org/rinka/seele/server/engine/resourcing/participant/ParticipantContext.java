/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing.participant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.rinka.seele.server.engine.resourcing.RSContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class : ParticipantContext
 * Usage :
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParticipantContext extends RSContext {

    public static final String DESC_NAME = "name";
    public static final String DESC_SKILL = "skill";
    public static final String DESC_REST_ENTRY = "rest_entry";
    public static final String DESC_REST_URI = "rest_uri";

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
     * Add a collection of skill to this participant.
     *
     * @param skills a List contains skill descriptors
     */
    public void addSkills(List<String> skills) {
        this.skill.addAll(skills);
    }
}
