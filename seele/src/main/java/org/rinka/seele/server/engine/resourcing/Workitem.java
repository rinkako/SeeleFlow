/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.context.RSContext;
import org.rinka.seele.server.engine.resourcing.context.TaskContext;
import org.rinka.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleWorkitemRepository;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class : Workitem
 * Usage :
 */
@Slf4j
public class Workitem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private ResourcingStateType state;

    @Getter
    private String namespace;

    @Getter
    private String wid;

    @Getter
    private String skill;

    @Getter
    private Map<String, Object> args;

    @JsonIgnore
    @Getter
    private SeeleWorkitemEntity entity;

    // TODO
    @JsonIgnore
    @Getter
    private Object process;

    /**
     * Has final result already posted to supervisor and get ACK
     */
    private Boolean supervisorAcknowledged = false;

    public static Workitem createFrom(TaskContext task, SeeleWorkitemRepository repository) throws Exception {
        Workitem workitem = new Workitem();
        workitem.namespace = task.getNamespace();
        workitem.args = task.getArgs();
        workitem.process = null;  // todo
        workitem.skill = task.getSkill();

        SeeleWorkitemEntity swe = new SeeleWorkitemEntity();
        swe.setRequestId(task.getRequestId());
        swe.setTaskId(0);  // todo
        swe.setArguments(JsonUtil.dumps(task.getArgs()));
        swe.setWid(UUID.randomUUID().toString());
        workitem.entity = repository.save(swe);
        workitem.wid = workitem.entity.getWid();

        return workitem;
    }

    public enum ResourcingStateType {
        /**
         * Arrive at Seele, but never handle
         */
        CREATED,

        /**
         * Resourcing finished, notified worker for handle
         */
        ALLOCATED,

        /**
         * Resourcing failed
         */
        BAD_ALLOCATED,

        /**
         * Workitem already cancelled
         */
        CANCELLED,

        /**
         * Accepted by participant, but not handle yet
         */
        ACCEPTED,

        /**
         * Participant already fired the workitem, waiting for complete
         */
        RUNNING,

        /**
         * Workitem finished with any exception
         */
        EXCEPTION,

        /**
         * Workitem was forced to be complete by supervisor request
         */
        FORCE_COMPLETED,

        /**
         * Workitem completed normally
         */
        COMPLETED
    }

}
