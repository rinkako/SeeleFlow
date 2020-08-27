/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.yurily.seele.server.connect.ws;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.yurily.seele.server.connect.ws.listener.*;
import org.yurily.seele.server.engine.resourcing.participant.agent.MetadataPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Class : SeeleSocketIOServer
 * Usage :
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class SeeleSocketIOServer {

    public static final String HEADER_Namespace = "namespace";
    public static final String HEADER_AuthToken = "token";
    public static final String HEADER_ParticipantId = "participant-id";

    public static final String EVENT_RSSystemRequest = "__WF_RS_SYSTEM_EVT__";
    public static final String EVENT_RSSystemResponse = "__WF_RS_SYSTEM_RESPONSE_EVT__";
    public static final String EVENT_RSParticipantRequest = "__WF_RS_PARTICIPANT_EVT__";
    public static final String EVENT_RSParticipantResponse = "__WF_RS_PARTICIPANT_RESPONSE_EVT__";
    public static final String EVENT_RSEvent = "__WF_RS_DISPATCH_EVT__";
    public static final String EVENT_RSRequireMeta = "__WF_RS_REQUIRE_META__";
    public static final String EVENT_RSResponseMeta = "__WF_RS_RESPONSE_META__";
    public static final String EVENT_HeartBeatEvent = "__WF_HEARTBEAT_CLIENT_EVT__";
    public static final String EVENT_HeartBeatResponseEvent = "__WF_HEARTBEAT_RESPONSE_EVT__";
    public static final String EVENT_RSParticipantLogging = "__WF_PARTICIPANT_LOGGING__";

    public static final String BODY_ServerID = "server-id";

    @Autowired
    private ParticipantAuthListener authListener;

    @Autowired
    private ParticipantConnectInListener connectListener;

    @Autowired
    private ParticipantDisconnectListener disconnectListener;

    @Autowired
    private ParticipantHeartbeatListener heartbeatListener;

    @Autowired
    private ParticipantMetaListener metaListener;

    @Autowired
    private ParticipantSystemListener systemListener;

    @Autowired
    private ParticipantDataListener eventListener;

    @Autowired
    private ParticipantWorkitemLoggingListener workitemLoggingListener;

    @Value("${seele.participant.ws.port}")
    private Integer listenPort;

    private SocketIOServer server;

    @PostConstruct
    public void init() {
        Configuration config = new Configuration();
        config.setPort(this.listenPort);
        config.setHostname("localhost");
        config.setAuthorizationListener(this.authListener);
        this.server = new SocketIOServer(config);
        this.server.addConnectListener(this.connectListener);
        this.server.addDisconnectListener(this.disconnectListener);
        this.server.addEventListener(EVENT_RSParticipantRequest, String.class, this.eventListener);
        this.server.addEventListener(EVENT_HeartBeatEvent, String.class, this.heartbeatListener);
        this.server.addEventListener(EVENT_RSSystemResponse, String.class, this.systemListener);
        this.server.addEventListener(EVENT_RSResponseMeta, MetadataPackage.class, this.metaListener);
        this.server.addEventListener(EVENT_RSParticipantLogging, String.class, this.workitemLoggingListener);
        this.server.start();
    }
}
