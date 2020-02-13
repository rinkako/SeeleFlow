/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.context.TaskContext;
import org.rinka.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleWorkitemRepository;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Class : Workitem
 * Usage :
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class Workitem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private ResourcingStateType state;

    @Getter
    private String namespace;

    @Getter
    private String wid = null;

    @Getter
    private String skill;

    @Getter
    private Map<String, Object> args;

    @Getter
    private String requestId;

    @Getter
    private String taskName;

    @JsonIgnore
    @Getter
    private SeeleWorkitemEntity entity;

    // TODO
    @JsonIgnore
    @Getter
    private Object process;

    @Getter
    @Setter
    private Timestamp createTime;

    @Getter
    @Setter
    private Timestamp enableTime;

    @Getter
    @Setter
    private Timestamp startTime;

    @Getter
    @Setter
    private Timestamp completeTime;

    @JsonIgnore
    private transient SeeleWorkitemRepository repository;

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
        workitem.state = ResourcingStateType.CREATED;
        workitem.requestId = task.getRequestId();
        workitem.repository = repository;
        workitem.createTime = Timestamp.from(ZonedDateTime.now().toInstant());
        workitem.taskName = task.getTaskName();
        workitem.flushSteady();
        return workitem;
    }

    @Transactional
    public void markBadAllocated() throws Exception {
        this.state = ResourcingStateType.BAD_ALLOCATED;
        this.flushSteady();
    }

    @Transactional
    public void flushSteady() throws Exception {
        if (this.wid == null) {
            SeeleWorkitemEntity swe = new SeeleWorkitemEntity();
            swe.setRequestId(this.requestId);
            swe.setTaskId(0);  // todo
            swe.setArguments(JsonUtil.dumps(this.args));
            swe.setWid(UUID.randomUUID().toString());
            swe.setState(this.state.name());
            swe.setCreateTime(this.createTime);
            swe.setTaskName(this.taskName);
            this.entity = this.repository.save(swe);
            this.wid = this.entity.getWid();
        } else {
            this.entity.setState(this.state.name());
            this.entity.setArguments(JsonUtil.dumps(this.args));
            this.entity.setCreateTime(this.createTime);
            this.entity.setEnableTime(this.enableTime);
            this.entity.setStartTime(this.startTime);
            this.entity.setCompleteTime(this.completeTime);
            this.repository.save(this.entity);
        }
    }

    @Transactional
    public void refreshSteady() {

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
