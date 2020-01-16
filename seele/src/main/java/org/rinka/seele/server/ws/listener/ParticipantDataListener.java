/*
 * Author : Rinka
 * Date   : 2020/1/16
 * Contact: gzlinjia@corp.netease.com
 */
package org.rinka.seele.server.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantDataListener
 * Usage :
 */
@Component
public class ParticipantDataListener implements DataListener<String> {
    private static final Logger logger = LoggerFactory.getLogger(ParticipantDataListener.class);

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        logger.info(data);
    }
}
