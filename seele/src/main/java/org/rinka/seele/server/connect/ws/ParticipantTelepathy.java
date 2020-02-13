/*
 * Author : Rinka
 * Date   : 2020/2/13
 */
package org.rinka.seele.server.connect.ws;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.*;
import org.rinka.seele.server.GDP;
import org.rinka.seele.server.engine.resourcing.Workitem;
import org.rinka.seele.server.engine.resourcing.participant.ParticipantContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class : ParticipantTelepathy
 * Usage :
 */
@Component
public class ParticipantTelepathy {

    public void NotifyWorkitemAllocated(ParticipantContext participant, Workitem workitem) {
        ParticipantMail mail = new ParticipantMail();
        mail.setWorkitemId(workitem.getWid());
        mail.setPreviousState("NONE");
        mail.setState(workitem.getState().name());
        mail.setHint("allocate");
        mail.setTaskName(workitem.getTaskName());
        SocketIOClient participantSG = ParticipantSocketPool.get(workitem.getNamespace(), participant.getParticipantId());
        participantSG.sendEvent(SeeleSocketIOServer.EVENT_RSEvent, mail);
    }

    @Data
    @ToString
    @EqualsAndHashCode
    public static class ParticipantMail implements Serializable {
        private static final long serialVersionUID = 1L;

        private String nodeId = GDP.SeeleId;

        private String hint;

        private String taskName;

        private String workitemId;

        private String previousState;

        private String state;

        private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
