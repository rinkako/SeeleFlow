/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.rinka.seele.server.ws.listener;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantAuthListener
 * Usage :
 */
@Component
public class ParticipantAuthListener implements AuthorizationListener {

    /**
     * Checks is client with handshake data is authorized
     *
     * @param data - handshake data
     * @return - <b>true</b> if client is authorized of <b>false</b> otherwise
     */
    @Override
    public boolean isAuthorized(HandshakeData data) {
        String token = data.getHttpHeaders().get("token");
        if (token == null) {
            return false;
        } else {
            // TODO
            return true;
        }
    }
}
