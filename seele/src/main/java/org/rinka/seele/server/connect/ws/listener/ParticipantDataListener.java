/*
 * Author : Rinka
 * Date   : 2020/1/16
 */
package org.rinka.seele.server.connect.ws.listener;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.RSInteraction;
import org.rinka.seele.server.engine.resourcing.context.ResourcingStateType;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantPool;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class : ParticipantDataListener
 * Usage :
 */
@Slf4j
@Component
public class ParticipantDataListener implements DataListener<String> {

    @Autowired
    private RSInteraction interaction;

    private static final String MESSAGE_OUT_OF_MANAGE = "OUT_OF_SERVER_MANAGEMENT";

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        ParticipantRequestMail mail = JsonUtil.parseRaw(data, new TypeReference<ParticipantRequestMail>() {});
        ParticipantContext pc = ParticipantPool.getParticipantBySessionId(client.getSessionId().toString());
        if (pc == null) {
            log.warn("request in but Participant context not exist, waiting for metadata");
            ackSender.sendAckData(MESSAGE_OUT_OF_MANAGE);
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
                this.interaction.completeWorkitemByParticipant(mail.epochId, workitem, participant);
                break;
        }
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
