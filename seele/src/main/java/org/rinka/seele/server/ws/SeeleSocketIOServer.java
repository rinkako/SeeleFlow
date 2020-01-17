/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.rinka.seele.server.ws;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.rinka.seele.server.ws.listener.ParticipantAuthListener;
import org.rinka.seele.server.ws.listener.ParticipantConnectInListener;
import org.rinka.seele.server.ws.listener.ParticipantDataListener;
import org.rinka.seele.server.ws.listener.ParticipantDisconnectListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Class : SeeleSocketIOServer
 * Usage :
 */
@Component
public class SeeleSocketIOServer {

    @Autowired
    private ParticipantAuthListener authListener;

    @Autowired
    private ParticipantConnectInListener connectListener;

    @Autowired
    private ParticipantDisconnectListener disconnectListener;

    @Autowired
    private ParticipantDataListener eventListener;

    @Value("${server.port}")
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
        this.server.addEventListener("__WF_RS_RESPONSE_META__", String.class, this.eventListener);
        this.server.addEventListener("__WF_HEARTBEAT_CLIENT_EVT__", String.class, this.eventListener);
        this.server.start();
    }
}
