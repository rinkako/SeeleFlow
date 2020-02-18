/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.rinka.seele.sdk.javaagent.sdk;

import lombok.Data;

import java.io.Serializable;

/**
 * Class : WorkflowSystemEvent
 * Usage :
 */
@Data
public class WorkflowSystemEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    static final String Action_PauseNode = "pause";
    static final String Action_ResumeNode = "resume";

    private String action;

    static WorkflowSystemEvent of(Object... payload) {
        WorkflowSystemEvent wse = new WorkflowSystemEvent();
        return wse;
    }

    private WorkflowSystemEvent() {}
}
