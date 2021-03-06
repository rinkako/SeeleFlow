/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.yurily.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.connect.ws.ParticipantSocketPool;
import org.yurily.seele.server.connect.ws.SeeleSocketIOServer;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantPool;
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
        HttpHeaders headers = client.getHandshakeData().getHttpHeaders();
        String namespace = headers.get(SeeleSocketIOServer.HEADER_Namespace);
        String participantId = headers.get(SeeleSocketIOServer.HEADER_ParticipantId);
        if (StringUtils.isEmpty(participantId)) {
            log.warn("A participant disconnect from Seele, but participant-id not found");
        } else {
            ParticipantPool.removeParticipantBySessionId(client.getSessionId().toString());
            ParticipantSocketPool.remove(namespace, participantId);
        }
        log.info("A participant disconnected from Seele-Server: " + client.getSessionId());
    }
}
