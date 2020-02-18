/*
 * Author : Rinka
 * Date   : 2020/2/14
 */
package org.rinka.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantPool;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Class : ParticipantWorkitemLoggingListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantWorkitemLoggingListener implements DataListener<String> {

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) {
        try {
            Map parsedLog = JsonUtil.parse(data, Map.class);
            String wid = (String) parsedLog.get("wid");
            Boolean bulk = parsedLog.get("bulk").equals("true");
            String content = (String) parsedLog.get("log");
            // TODO DEBUG
            log.info(String.format("Workitem[%s] logging: %s", wid, content));
            WorkitemContext workitem = WorkitemContext.loadByWid(wid);
            if (bulk) {
                List<String> lines = JsonUtil.parseRaw(content, new TypeReference<List<String>>() {});
                lines.forEach(workitem::appendLogLine);
            } else {
                workitem.appendLogLine(content);
            }
        } catch (JsonProcessingException e) {
            ParticipantContext pc = ParticipantPool.getParticipantBySessionId(client.getSessionId().toString());
            if (pc != null) {
                log.error("cannot parse log item from participant: " + pc.toString());
            } else {
                log.error(String.format("unknown participant[%s] with parsing exception: %s",
                        client.getSessionId().toString(), e.getMessage()));
            }
        }
    }
}
