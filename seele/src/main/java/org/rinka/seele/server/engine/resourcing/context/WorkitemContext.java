/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.principle.Principle;
import org.rinka.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleWorkitemRepository;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : Workitem
 * Usage :
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class WorkitemContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, WorkitemContext>> cachePool = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, WorkitemContext> workitemPool = new ConcurrentHashMap<>();

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

    @Getter
    private Principle principle;

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

    public static WorkitemContext createFrom(TaskContext task, SeeleWorkitemRepository repository) throws Exception {
        WorkitemContext workitem = new WorkitemContext();
        workitem.namespace = task.getNamespace();
        workitem.args = task.getArgs();
        workitem.process = null;  // todo
        workitem.skill = task.getSkill();
        workitem.state = ResourcingStateType.CREATED;
        workitem.requestId = task.getRequestId();
        workitem.repository = repository;
        workitem.createTime = Timestamp.from(ZonedDateTime.now().toInstant());
        workitem.taskName = task.getTaskName();
        workitem.principle = task.getPrinciple();
        workitem.flushSteady();
        workitem.addSelfToCache();
        return workitem;
    }

    public static WorkitemContext loadByWid(String wid) {
        WorkitemContext workitem = WorkitemContext.getFromCache(wid);
        if (workitem == null) {
            workitem = new WorkitemContext();
            workitem.wid = wid;
            workitem.refreshSteady();
            workitem.addSelfToCache();
        }
        return workitem;
    }

    public static WorkitemContext loadByNamespaceAndWid(String namespace, String wid) {
        WorkitemContext workitem = WorkitemContext.getFromCache(namespace, wid);
        if (workitem == null) {
            workitem = new WorkitemContext();
            workitem.wid = wid;
            workitem.refreshSteady();
            workitem.addSelfToCache();
        }
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
            this.entity = this.repository.findByWid(this.wid);
            this.entity.setState(this.state.name());
            this.entity.setArguments(JsonUtil.dumps(this.args));
            this.entity.setCreateTime(this.createTime);
            this.entity.setEnableTime(this.enableTime);
            this.entity.setStartTime(this.startTime);
            this.entity.setCompleteTime(this.completeTime);
            this.repository.saveAndFlush(this.entity);
        }
    }

    @Transactional
    public void refreshSteady() {
        if (this.wid == null) {
            log.error("cannot refresh because `wid` is null");
            return;
        }
        SeeleWorkitemEntity swe = this.repository.findByWid(this.wid);
        if (swe == null) {
            log.error("cannot refresh workitem since not exist steady");
            return;
        }
        this.entity = swe;
        this.requestId = swe.getRequestId();
        this.createTime = swe.getCreateTime();
        this.enableTime = swe.getEnableTime();
        this.startTime = swe.getStartTime();
        this.completeTime = swe.getCompleteTime();
        try {
            this.args = JsonUtil.parse(swe.getArguments(), Map.class);
        } catch (JsonProcessingException e) {
            log.warn("cannot parse args: " + e.getMessage());
        }
        this.taskName = swe.getTaskName();
        this.state = Enum.valueOf(ResourcingStateType.class, swe.getState());
    }

    private void addSelfToCache() {
        ConcurrentHashMap<String, WorkitemContext> namespacedPool = WorkitemContext
                .cachePool.computeIfAbsent(this.namespace, ns -> new ConcurrentHashMap<>());
        namespacedPool.put(this.wid, this);
        WorkitemContext.workitemPool.put(this.wid, this);
    }

    public void removeSelfFromCache() {
        WorkitemContext.removeCache(this.wid);
    }

    private static WorkitemContext getFromCache(String namespace, String wid) {
        ConcurrentHashMap<String, WorkitemContext> namespacedPool = WorkitemContext
                .cachePool.computeIfAbsent(namespace, ns -> new ConcurrentHashMap<>());
        return namespacedPool.get(wid);
    }

    private static WorkitemContext getFromCache(String wid) {
        return WorkitemContext.workitemPool.get(wid);
    }

    private static WorkitemContext removeCache(String wid) {
        WorkitemContext rm = WorkitemContext.workitemPool.remove(wid);
        if (rm != null) {
            ConcurrentHashMap<String, WorkitemContext> ns = WorkitemContext.cachePool.get(rm.namespace);
            if (ns != null) {
                ns.remove(wid);
            }
        }
        return rm;
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
