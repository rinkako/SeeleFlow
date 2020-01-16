/*
 * Author : Rinka
 * Date   : 2020/1/16
 * Contact: gzlinjia@corp.netease.com
 */
package org.rinka.seele.server.ws.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantDisconnectListener
 * Usage :
 */
@Component
public class ParticipantDisconnectListener implements DisconnectListener {
    private static final Logger logger = LoggerFactory.getLogger(ParticipantDisconnectListener.class.getName());

    @Override
    public void onDisconnect(SocketIOClient client) {
        logger.info("A participant disconnected from Seele-Server: " + client.getSessionId());
    }
}
