/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.yurily.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.GDP;
import org.yurily.seele.server.engine.resourcing.RSInteraction;
import org.yurily.seele.server.engine.resourcing.context.ResourcingStateType;
import org.yurily.seele.server.engine.resourcing.context.WorkitemContext;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantContext;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantPool;
import org.yurily.seele.server.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class : ParticipantDataListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantDataListener implements DataListener<String> {

    @Autowired
    private RSInteraction interaction;

    private static final String MESSAGE_ACK = "ACK";
    private static final String MESSAGE_OUT_OF_MANAGE = "OUT_OF_SERVER_MANAGEMENT";

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        ParticipantRequestMail mail = JsonUtil.parseRaw(data, new TypeReference<ParticipantRequestMail>() {});
        ParticipantContext pc = ParticipantPool.getParticipantBySessionId(client.getSessionId().toString());
        if (pc == null) {
            log.warn("request in but Participant context not exist, waiting for metadata");
            SeeleRequestMail srMail = new SeeleRequestMail();
            srMail.setContent(MESSAGE_OUT_OF_MANAGE);
            try {
                String jStr = JsonUtil.dumps(srMail);
                ackSender.sendAckData(jStr);
            } catch (JsonProcessingException e) {
                log.error("cannot jsonify string: " + e.getMessage());
            }
            return;
        }
        log.info(String.format("Mail from participant[%s][%s]: %s", pc.getNamespace(), pc.getDisplayName(), mail.toString()));
        ResourcingStateType rst = Enum.valueOf(ResourcingStateType.class, mail.targetState);
        WorkitemContext workitem = WorkitemContext.loadByNamespaceAndWid(mail.namespace, mail.workitemId);
        ParticipantContext participant = ParticipantPool.getParticipantBySessionId(client.getSessionId().toString());
        switch (rst) {
            case ACCEPTED:
                this.interaction.acceptWorkitemByParticipant(mail.epochId, workitem, participant);
                break;
            case RUNNING:
                this.interaction.startWorkitemByParticipant(mail.epochId, workitem, participant);
                break;
            case COMPLETED:
                this.interaction.completeOrExceptionWorkitemByParticipant(mail.epochId, workitem, participant, false);
                break;
            case EXCEPTION:
                this.interaction.completeOrExceptionWorkitemByParticipant(mail.epochId, workitem, participant, true);
                break;
        }
        // ack
        SeeleRequestMail srMail = new SeeleRequestMail();
        srMail.setContent(MESSAGE_ACK);
        try {
            String jStr = JsonUtil.dumps(srMail);
            ackSender.sendAckData(jStr);
        } catch (JsonProcessingException e) {
            log.error("cannot jsonify string: " + e.getMessage());
        }
    }

    @Data
    @ToString
    @EqualsAndHashCode
    private static class SeeleRequestMail {

        private String seeleId = GDP.SeeleId;

        private String content;

        private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Data
    @ToString
    @EqualsAndHashCode
    private static class ParticipantRequestMail {

        private int epochId;

        private String namespace;

        private String workitemId;

        private String targetState;

        private String timestamp;
    }
}
