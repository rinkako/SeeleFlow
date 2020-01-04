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

/**
 * Class : ParticipantContext
 * Usage :
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParticipantContext extends RSContext {

    /**
     * Participant global id.
     */
    private String participantId;

    /**
     * User-friendly resource name.
     */
    private String displayName;

    /**
     * Type of the resource.
     */
    private ParticipantType type;

    /**
     * A descriptor guides where RS to find this resource.
     *   Agent - usually a hostname with port
     *   Human - maybe a string describe his position in organization
     *   SubProcess - process global id
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
}
