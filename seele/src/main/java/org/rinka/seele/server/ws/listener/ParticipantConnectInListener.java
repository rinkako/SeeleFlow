/*
 * Author : Rinka
 * Date   : 2020/1/16
 * Contact: gzlinjia@corp.netease.com
 */
package org.rinka.seele.server.ws.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantConnectInListener
 * Usage :
 */
@Component
public class ParticipantConnectInListener implements ConnectListener {
    private static final Logger logger = LoggerFactory.getLogger(ParticipantConnectInListener.class.getName());

    @Override
    public void onConnect(SocketIOClient client) {
        logger.info("A participant connected to Seele-Server: " + client.getSessionId());
        client.sendEvent("connected", "hello");
    }
}
