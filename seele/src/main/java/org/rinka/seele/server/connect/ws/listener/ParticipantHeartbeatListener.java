/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.rinka.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.GDP;
import org.rinka.seele.server.connect.ws.SeeleSocketIOServer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Class : ParticipantHeartbeatListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantHeartbeatListener implements DataListener<String> {

    private static final Map<String, String> standardPayload = new HashMap<>();

    /**
     * Invokes when data object received from client
     */
    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        log.info(String.format("Received heartbeat from participant agent: %s", client.getSessionId()));
        Map<String, Object> resp = new HashMap<>(ParticipantHeartbeatListener.standardPayload);
        client.sendEvent(SeeleSocketIOServer.EVENT_HeartBeatResponseEvent, resp);
    }

    @PostConstruct
    private void init() {
        ParticipantHeartbeatListener.standardPayload.put(SeeleSocketIOServer.BODY_ServerID, GDP.SeeleId);
    }
}
