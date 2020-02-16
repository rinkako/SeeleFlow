/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.rinka.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.connect.ws.ParticipantSocketPool;
import org.rinka.seele.server.connect.ws.SeeleSocketIOServer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Class : ParticipantConnectInListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantConnectInListener implements ConnectListener {

    @Override
    public void onConnect(SocketIOClient client) {
        log.info(String.format("A participant connected to Seele-Server: %s (%s | %s)",
                client.getSessionId(), client.getRemoteAddress().toString(), client.getTransport().getValue()));
        HttpHeaders headers = client.getHandshakeData().getHttpHeaders();
        String namespace = headers.get(SeeleSocketIOServer.HEADER_Namespace);
        String participantId = headers.get(SeeleSocketIOServer.HEADER_ParticipantId);
        if (StringUtils.isEmpty(participantId)) {
            log.warn("A participant connect without participant-id, will be force disconnect from Seele");
            client.disconnect();
        } else {
            ParticipantSocketPool.add(namespace, participantId, client);
            client.sendEvent("connected", "Vollerei!");
            log.info("require meta for client: " + client.getSessionId());
            client.sendEvent(SeeleSocketIOServer.EVENT_RSRequireMeta, "meta need");
        }
    }
}
