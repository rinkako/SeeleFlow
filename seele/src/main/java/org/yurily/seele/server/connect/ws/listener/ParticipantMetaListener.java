/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.yurily.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantPool;
import org.yurily.seele.server.engine.resourcing.participant.agent.MetadataPackage;
import org.yurily.seele.server.connect.ws.SeeleSocketIOServer;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantMetaListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantMetaListener implements DataListener<MetadataPackage> {

    /**
     * Invokes when data object received from client
     */
    @Override
    public void onData(SocketIOClient client, MetadataPackage data, AckRequest ackSender) throws Exception {
        log.info(String.format("Received metadata report from participant agent: %s", client.getSessionId()));
        HttpHeaders headers = client.getHandshakeData().getHttpHeaders();
        String namespace = headers.get(SeeleSocketIOServer.HEADER_Namespace);
        String participantId = headers.get(SeeleSocketIOServer.HEADER_ParticipantId);
        ParticipantPool.namespace(namespace).addAgentParticipant(client.getSessionId().toString(), participantId, data);
    }
}
