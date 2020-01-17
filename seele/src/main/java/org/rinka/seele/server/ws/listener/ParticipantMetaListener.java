/*
 * Author : Rinka
 * Date   : 2020/1/17
 */
package org.rinka.seele.server.ws.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantMetaListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantMetaListener implements DataListener<String> {

    /**
     * Invokes when data object received from client
     */
    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        log.info(String.format("Received heartbeat from participant agent: %s", client.getSessionId()));
        JSONObject meta = JSON.parseObject(data);

    }
}
