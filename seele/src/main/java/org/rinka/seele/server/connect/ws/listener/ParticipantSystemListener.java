/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.rinka.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantSystemListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantSystemListener implements DataListener<String> {

    /**
     * Invokes when data object received from client
     */
    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        log.info(String.format("Received heartbeat from participant agent: %s", client.getSessionId()));

    }
}
