/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.yurily.seele.sdk.javaagent;

import io.socket.client.Socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : WorkFlowClientContext
 * Usage : 工作流客户端的上下文
 */
public class WorkFlowClientContext {

    public Socket webSocket;

    public final String namespace;
    public final String uniqueId;
    public final String displayName;

    public final int maxConcurrentSize;

    public Set<String> resourceServiceHosts;

    public Set<String> serviceTags;

    public IMetadataReportFunction metadataFunction;

    public List<IHeartbeatFunction> heartbeatFunctions = new ArrayList<>();

    public List<IHeartbeatResponseFunction> heartbeatResponseFunctions = new ArrayList<>();

    public ConcurrentHashMap<String, IWorkitemHandler> handlerContainer = new ConcurrentHashMap<>();

    public ParticipantType participantType = null;

    public ParticipantServiceStateType workingState = ParticipantServiceStateType.NotService;

    public ReentrantType reentrantType = ReentrantType.NotReentrant;

    public WorkFlowClientContext(String namespace, String uniqueId, String displayName, int maxConcurrentSize) {
        this.namespace = namespace;
        this.uniqueId = uniqueId;
        this.displayName = displayName;
        this.maxConcurrentSize = maxConcurrentSize;
    }

    public void registerWorkitemHandler(String jobType, IWorkitemHandler handler) {
        this.handlerContainer.put(jobType, handler);
    }

    public enum ParticipantType {
        Supervisor,
        Agent,
        Human
    }

    public enum ParticipantServiceStateType {
        NotService,
        Working,
        Paused
    }

    public enum ReentrantType {
        Reentrant,
        NotReentrant
    }
}
