/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.rinka.seele.server.ws.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.ws.ParticipantSocketPool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Class : ParticipantConnectInListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantConnectInListener implements ConnectListener {

    private static final String HEADER_ParticipantId = "participant-id";

    @Override
    public void onConnect(SocketIOClient client) {
        log.info("A participant connected to Seele-Server: " + client.getSessionId());
        String participantId = client.getHandshakeData().getHttpHeaders().get(HEADER_ParticipantId);
        if (StringUtils.isEmpty(participantId)) {
            log.warn("A participant connect without participant-id, will be force disconnect from Seele");
            client.disconnect();
        } else {
            ParticipantSocketPool.add(participantId, client);
            client.sendEvent("connected", "hello");
        }
    }
}
