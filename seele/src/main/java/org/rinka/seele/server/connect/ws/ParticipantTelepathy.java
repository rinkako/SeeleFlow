/*
 * Author : Rinka
 * Date   : 2020/2/13
 */
package org.rinka.seele.server.connect.ws;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.GDP;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Class : ParticipantTelepathy
 * Usage :
 */
@Slf4j
@Component
public class ParticipantTelepathy {

    public void NotifyWorkitemAllocated(ParticipantContext participant, WorkitemContext workitem) {
        SeeleWorkitemNotifyMail mail = new SeeleWorkitemNotifyMail();
        mail.setWorkitemId(workitem.getWid());
        mail.setPreviousState("NONE");
        mail.setState(workitem.getState().name());
        mail.setHint("allocate");
        mail.setTaskName(workitem.getTaskName());
        mail.setNamespace(workitem.getNamespace());
        mail.setRequestId(workitem.getRequestId());
        mail.setArgs(workitem.getArgs());
        SocketIOClient participantSG = ParticipantSocketPool.get(workitem.getNamespace(), participant.getParticipantId());
        if (participantSG == null) {
            log.error("retrieve participant socket but null, did worker reconnected with new session id?");
        } else {
            participantSG.sendEvent(SeeleSocketIOServer.EVENT_RSEvent, mail);
        }
    }

    @Data
    @ToString
    @EqualsAndHashCode
    public static class SeeleWorkitemNotifyMail implements Serializable {
        private static final long serialVersionUID = 1L;

        private String requestId;

        private String nodeId = GDP.SeeleId;

        private String namespace;

        private String hint;

        private String taskName;

        private String workitemId;

        private String previousState;

        private String state;

        private Map<String, Object> args;

        private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
