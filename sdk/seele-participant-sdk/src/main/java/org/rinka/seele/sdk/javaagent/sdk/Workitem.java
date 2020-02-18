/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.rinka.seele.sdk.javaagent.sdk;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rinka.seele.sdk.javaagent.sdk.logger.WorkflowLoggerFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * Class : Workitem
 * Usage :
 */
@ToString
@EqualsAndHashCode
public class Workitem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private String namespace;

    @Getter
    private String nodeId;

    @Getter
    private String requestId;

    @Getter
    private String wid;

    @Getter
    private String taskName;

    @Getter
    private Map<String, Object> args;

    @Getter
    @Setter
    private ResourcingStateType state;

    @JsonIgnore
    @Getter
    private transient Logger logger;

    public static Workitem of(String namespace, String nodeId, String requestId, String wid, String taskName, Map<String, Object> args) throws Exception {
        Workitem workitem = new Workitem();
        workitem.namespace = namespace;
        workitem.nodeId = nodeId;
        workitem.requestId = requestId;
        workitem.wid = wid;
        workitem.taskName = taskName;
        workitem.logger = WorkflowLoggerFactory.getBulkLogger(namespace, wid);
        return workitem;
    }

    public enum ResourcingStateType {
        /**
         * Arrive at Seele, but never handle
         */
        CREATED,

        /**
         * Resourcing finished, notified worker for handle
         */
        ALLOCATED,

        /**
         * Resourcing failed
         */
        BAD_ALLOCATED,

        /**
         * Workitem already cancelled
         */
        CANCELLED,

        /**
         * Accepted by participant, but not handle yet
         */
        ACCEPTED,

        /**
         * Participant already fired the workitem, waiting for complete
         */
        RUNNING,

        /**
         * Workitem finished with any exception
         */
        EXCEPTION,

        /**
         * Workitem was forced to be complete by supervisor request
         */
        FORCE_COMPLETED,

        /**
         * Workitem completed normally
         */
        COMPLETED
    }
}
