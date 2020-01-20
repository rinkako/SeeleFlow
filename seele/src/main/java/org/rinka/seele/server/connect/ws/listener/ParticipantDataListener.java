/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.rinka.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantDataListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantDataListener implements DataListener<String> {

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {

        log.info(data);
    }
}
