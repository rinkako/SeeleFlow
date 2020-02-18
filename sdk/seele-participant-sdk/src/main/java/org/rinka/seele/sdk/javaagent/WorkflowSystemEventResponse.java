/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.rinka.seele.sdk.javaagent;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class : WorkflowSystemEventResponse
 * Usage :
 */
@Data
public class WorkflowSystemEventResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> payload = new HashMap<>();

    public void setPayloadTerm(String key, Object val) {
        this.payload.put(key, val);
    }
}
