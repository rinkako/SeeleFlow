/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.yurily.seele.server.engine.resourcing.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantContext;
import org.yurily.seele.server.engine.resourcing.participant.ParticipantPool;
import org.yurily.seele.server.engine.resourcing.principle.Principle;
import org.yurily.seele.server.engine.resourcing.queue.WorkQueue;
import org.yurily.seele.server.logging.RDBWorkitemLogger;
import org.yurily.seele.server.logging.WorkitemLogger;
import org.yurily.seele.server.steady.seele.entity.SeeleRawtaskEntity;
import org.yurily.seele.server.steady.seele.entity.SeeleTaskEntity;
import org.yurily.seele.server.steady.seele.entity.SeeleWorkitemEntity;
import org.yurily.seele.server.steady.seele.repository.SeeleWorkitemRepository;
import org.yurily.seele.server.util.JsonUtil;
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
@ToString(exclude = {"queueReference"})
@EqualsAndHashCode
public class WorkitemContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, WorkitemContext>> cachePool = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, WorkitemContext> WorkitemPool = new ConcurrentHashMap<>();

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

    @Getter
    @Setter
    @JsonIgnore
    private Timestamp markAsGarbageTime;

    @JsonIgnore
    @Getter
    private TaskContext taskTemplate;

    @SuppressWarnings({"rawtypes"})
    @Getter
    @JsonIgnore
    private WorkitemLogger logContainer;

    @JsonIgnore
    private static SeeleWorkitemRepository repository;

    @JsonIgnore
    @Getter
    private transient boolean logArrived = false;

    @JsonIgnore
    @Getter
    private transient boolean logFlushed = false;

    @JsonIgnore
    @Getter
    @Setter
    private transient WorkQueue queueReference;

    /**
     * Has final result already posted to supervisor and get ACK
     */
    private Boolean supervisorAcknowledged = false;

    public void appendLogLine(String log) {
        this.logContainer.append(log);
    }

    public void markLogAlreadyArrived() {
        this.logArrived = true;
    }

    public void markLogAlreadyFlushed() {
        this.logFlushed = true;
    }

    public void markLogNotFlush() {
        this.logFlushed = false;
    }

    public boolean isFinalState() {
        return WorkitemContext.isFinalState(this.state);
    }

    public static void bindingRepository(SeeleWorkitemRepository repository) {
        WorkitemContext.repository = repository;
    }

    public static WorkitemContext createFrom(TaskContext task) throws Exception {
        WorkitemContext workitem = new WorkitemContext();
        workitem.taskTemplate = task;
        workitem.namespace = task.getNamespace();
        workitem.state = ResourcingStateType.CREATED;
        workitem.requestId = task.getRequestId();
        workitem.createTime = Timestamp.from(ZonedDateTime.now().toInstant());
        workitem.principle = task.getPrinciple();
        if (task.getSubmitType() == TaskContext.ResourcingTaskSubmitType.RAW) {
            SeeleRawtaskEntity taskTpl = task.getRawEntity();
            workitem.process = null;
            workitem.skill = taskTpl.getSkill();
            workitem.taskName = taskTpl.getName();
            workitem.args = task.getCachedArgs();
            workitem.taskTemplate = TaskContext.getRawContextByRequestId(workitem.getNamespace(), workitem.getRequestId());
        } else {
            // todo
            SeeleTaskEntity taskTpl = task.getPermanentEntity();
            workitem.process = null;
            workitem.skill = taskTpl.getSkill();
            workitem.taskName = taskTpl.getName();
        }
        workitem.logContainer = new RDBWorkitemLogger();  // todo
        workitem.flushSteady();
        workitem.addSelfToCache();
        return workitem;
    }

    public static WorkitemContext createFrom(SeeleWorkitemEntity swe) {
        WorkitemContext workitem = new WorkitemContext();
        workitem.logContainer = new RDBWorkitemLogger();  // todo
        workitem.refreshSteady(swe);
        workitem.addSelfToCache();
        return workitem;
    }

    public static WorkitemContext loadByWid(String wid) {
        WorkitemContext workitem = WorkitemContext.getFromCache(wid);
        return WorkitemContext.syncSteady(wid, workitem);
    }

    public static WorkitemContext loadByNamespaceAndWid(String namespace, String wid) {
        WorkitemContext workitem = WorkitemContext.getFromCache(namespace, wid);
        return WorkitemContext.syncSteady(wid, workitem);
    }

    public static boolean isFinalState(ResourcingStateType state) {
        return state == ResourcingStateType.COMPLETED ||
                state == ResourcingStateType.EXCEPTION ||
                state == ResourcingStateType.CANCELLED ||
                state == ResourcingStateType.FORCE_COMPLETED;
    }

    public static boolean isActiveState(ResourcingStateType state) {
        return !WorkitemContext.isFinalState(state);
    }

    private static WorkitemContext syncSteady(String wid, WorkitemContext workitem) {
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
            TaskContext task = this.taskTemplate;
            if (task.getSubmitType() == TaskContext.ResourcingTaskSubmitType.RAW) {
                swe.setTaskId(task.getRawEntity().getId());
            } else {
                swe.setTaskId(task.getPermanentEntity().getId());
            }
            swe.setNamespace(this.namespace);
            swe.setArguments(JsonUtil.dumps(this.args));
            swe.setWid(UUID.randomUUID().toString());
            swe.setState(this.state.name());
            swe.setCreateTime(this.createTime);
            swe.setTaskName(this.taskName);
            swe.setTaskType(task.getSubmitType().name());
            if (this.queueReference != null) {
                swe.setQueueId(this.queueReference.getQueueId());
            } else {
                swe.setQueueId(null);
            }
            this.entity = WorkitemContext.repository.save(swe);
            this.wid = this.entity.getWid();
        } else {
            this.entity = WorkitemContext.repository.findByWid(this.wid);
            this.entity.setState(this.state.name());
            this.entity.setArguments(JsonUtil.dumps(this.args));
            this.entity.setCreateTime(this.createTime);
            this.entity.setEnableTime(this.enableTime);
            this.entity.setStartTime(this.startTime);
            this.entity.setCompleteTime(this.completeTime);
            if (this.queueReference != null) {
                this.entity.setQueueId(this.queueReference.getQueueId());
            } else {
                this.entity.setQueueId(null);
            }
            WorkitemContext.repository.saveAndFlush(this.entity);
        }
    }

    @Transactional
    public void refreshSteady() {
        if (this.wid == null) {
            log.error("cannot refresh because `wid` is null");
            return;
        }
        SeeleWorkitemEntity swe = WorkitemContext.repository.findByWid(this.wid);
        if (swe == null) {
            log.error("cannot refresh workitem since not exist steady");
            return;
        }
        this.refreshSteady(swe);
    }

    public void refreshSteady(SeeleWorkitemEntity swe) {
        this.entity = swe;
        this.wid = swe.getWid();
        this.namespace = swe.getNamespace();
        this.requestId = swe.getRequestId();
        this.createTime = swe.getCreateTime();
        this.enableTime = swe.getEnableTime();
        this.startTime = swe.getStartTime();
        this.completeTime = swe.getCompleteTime();
        TaskContext.ResourcingTaskSubmitType taskType = Enum.valueOf(TaskContext.ResourcingTaskSubmitType.class, swe.getTaskType());
        if (taskType == TaskContext.ResourcingTaskSubmitType.RAW) {
            this.taskTemplate = TaskContext.getRawContextByRequestId(this.namespace, this.requestId);
            this.principle = this.taskTemplate.getPrinciple();
        } else {
            // todo
        }
        try {
            this.args = JsonUtil.parse(swe.getArguments(), Map.class);
        } catch (JsonProcessingException e) {
            log.warn("cannot parse args: " + e.getMessage());
        }
        String queueId = swe.getQueueId();
        if (queueId != null) {
            WorkQueue.QueueMetaHint queueMeta = WorkQueue.parseQueueId(queueId);
            if (queueMeta != null) {
                ParticipantContext participant = ParticipantPool.namespace(queueMeta.getNamespace())
                        .getParticipant(queueMeta.getParticipantId());
                if (participant == null) {
                    log.debug("participant context not exist, transition will perform without queue update: " + this.getWid());
                } else {
                    try {
                        participant.getQueueContainer().addToQueue(this, queueMeta.getQueueType());
                    } catch (Exception qe) {
                        log.error("cannot add item to queue: " + qe.getMessage());
                    }
                }
            }
        }
        this.taskName = swe.getTaskName();
        this.state = Enum.valueOf(ResourcingStateType.class, swe.getState());
    }

    private void addSelfToCache() {
        ConcurrentHashMap<String, WorkitemContext> namespacedPool = WorkitemContext
                .cachePool.computeIfAbsent(this.namespace, ns -> new ConcurrentHashMap<>());
        namespacedPool.put(this.wid, this);
        WorkitemContext.WorkitemPool.put(this.wid, this);
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
        return WorkitemContext.WorkitemPool.get(wid);
    }

    private static WorkitemContext removeCache(String wid) {
        WorkitemContext rm = WorkitemContext.WorkitemPool.remove(wid);
        if (rm != null) {
            ConcurrentHashMap<String, WorkitemContext> ns = WorkitemContext.cachePool.get(rm.namespace);
            if (ns != null) {
                ns.remove(wid);
            }
        }
        return rm;
    }
}
