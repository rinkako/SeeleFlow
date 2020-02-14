/*
 * Author : Rinka
 * Date   : 2020/2/14
 */
package org.rinka.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * Class : ParticipantWorkitemLoggingListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantWorkitemLoggingListener implements DataListener<ParticipantWorkitemLoggingListener.RSLogEvent> {

    @Override
    public void onData(SocketIOClient client, RSLogEvent data, AckRequest ackSender) {
        log.info(String.format("Workitem[%s][%s] logging: %s", data.namespace, data.workitemId, data.content));
    }

    @Data
    @ToString
    @EqualsAndHashCode
    public static class RSLogEvent implements Serializable {
        private String namespace;

        private String workitemId;

        private String content;

        private String timestamp;

        public RSLogEvent(String descriptor) throws JsonProcessingException {
            Map payload = JsonUtil.parse(descriptor, Map.class);
            this.namespace = payload.get("namespace").toString();
            this.workitemId = payload.get("workitemId").toString();
            this.content = payload.get("content").toString();
            this.timestamp = payload.get("timestamp").toString();
        }
    }
}
