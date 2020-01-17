/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.rinka.seele.server.ws.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.ws.ParticipantSocketPool;
import org.rinka.seele.server.ws.SeeleSocketIOServer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Class : ParticipantDisconnectListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantDisconnectListener implements DisconnectListener {

    @Override
    public void onDisconnect(SocketIOClient client) {
        String participantId = client.getHandshakeData().getHttpHeaders().get(SeeleSocketIOServer.HEADER_ParticipantId);
        if (StringUtils.isEmpty(participantId)) {
            log.warn("A participant disconnect from Seele, but participant-id not found");
        } else {
            ParticipantSocketPool.remove(participantId);
        }
        log.info("A participant disconnected from Seele-Server: " + client.getSessionId());
    }
}
