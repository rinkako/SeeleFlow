/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.rinka.seele.server.engine.resourcing.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.GDP;
import org.rinka.seele.server.engine.resourcing.principle.Principle;
import org.rinka.seele.server.steady.seele.entity.SeeleRawtaskEntity;
import org.rinka.seele.server.steady.seele.entity.SeeleTaskEntity;
import org.rinka.seele.server.steady.seele.repository.SeeleRawtaskRepository;
import org.rinka.seele.server.steady.seele.repository.SeeleTaskRepository;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : TaskContext
 * Usage :
 */
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskContext extends RSContext {

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, TaskContext>> permanentCachePool = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, TaskContext>> rawCachePool = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private String requestId;

    @Getter
    @Setter
    private ResourcingTaskSubmitType submitType;

    @Getter
    @Setter
    private SeeleTaskEntity permanentEntity;

    @Getter
    @Setter
    private SeeleRawtaskEntity rawEntity;

    @Getter
    private Principle principle;

    @Getter
    @Setter
    private Map<String, Object> cachedArgs;

    @JsonIgnore
    private transient SeeleTaskRepository permanentRepository;

    @JsonIgnore
    private transient SeeleRawtaskRepository rawRepository;

    public static TaskContext createRawFrom(String requestId,
                                            String namespace,
                                            String supervisorId,
                                            String taskName,
                                            Principle principle,
                                            String skill,
                                            Map<String, Object> args,
                                            SeeleRawtaskRepository repository) {
        return TaskContext.rawCachePool
                .computeIfAbsent(namespace, ns -> new ConcurrentHashMap<>())
                .computeIfAbsent(requestId, tn -> {
                    TaskContext task = new TaskContext();
                    SeeleRawtaskEntity taskEntity = repository.findByRequestId(requestId);
                    if (taskEntity == null) {
                        taskEntity = new SeeleRawtaskEntity();
                        taskEntity.setCreator(GDP.SeeleId);
                        taskEntity.setRequestId(requestId);
                        taskEntity.setNamespace(namespace);
                        taskEntity.setName(taskName);
                        taskEntity.setSubmitter(supervisorId);
                        taskEntity.setSkill(skill);
                        taskEntity.setPrinciple(principle.getDescriptor());
                        taskEntity.setDocumentation("");
                        taskEntity.setHooks("{}");
                        taskEntity.setEventCallbackMask("[]");
                        try {
                            taskEntity.setArguments(JsonUtil.dumps(args));
                        } catch (JsonProcessingException e) {
                            taskEntity.setArguments("{}");
                            log.error("cannot dump argument for task: " + e.getMessage());
                        }
                        taskEntity = repository.save(taskEntity);
                    } else {
                        log.warn("raw submit with duplicate requestId, although the workitem will be generated and resourcing normally");
                    }
                    task.setSubmitType(ResourcingTaskSubmitType.RAW);
                    task.setRequestId(requestId);
                    task.setRawEntity(taskEntity);
                    task.setNamespace(namespace);
                    task.cachedArgs = args;
                    task.principle = principle;
                    task.rawRepository = repository;
                    task.permanentRepository = null;
                    return task;
                });
    }

    @Transactional
    public void markAsFinish() {
        if (this.submitType == ResourcingTaskSubmitType.RAW) {
            this.rawEntity.setFinishTime(Timestamp.from(ZonedDateTime.now().toInstant()));
            this.flushSteady();
            this.removeSelfFromCache();
        }
    }

    @Transactional
    public void flushSteady() {
        if (this.submitType == ResourcingTaskSubmitType.RAW) {
            this.rawRepository.save(this.rawEntity);
        } else {
            this.permanentRepository.save(this.permanentEntity);
        }
    }

    @Transactional
    public void refreshSteady() {
        String ns = this.getNamespace();
        if (this.submitType == ResourcingTaskSubmitType.RAW) {
            this.rawEntity = this.rawRepository.findByRequestId(this.requestId);
        } else {
            String taskName = this.permanentEntity.getName();
            this.permanentEntity = this.permanentRepository.findByNamespaceAndName(ns, taskName);
        }
    }

    public void removeSelfFromCache() {
        if (this.submitType == ResourcingTaskSubmitType.RAW) {
            ConcurrentHashMap<String, TaskContext> namespaced = TaskContext.rawCachePool.get(this.getNamespace());
            if (namespaced != null) {
                namespaced.remove(this.requestId);
            }
        } else {
            ConcurrentHashMap<String, TaskContext> namespaced = TaskContext.permanentCachePool.get(this.getNamespace());
            if (namespaced != null) {
                namespaced.remove(this.permanentEntity.getName());
            }
        }
    }

    private TaskContext() {
    }

    public enum ResourcingTaskSubmitType {
        /**
         * directly submit by supervisor without any belonging active procedure
         */
        RAW,

        /**
         * submit by engine internally with a belonging active procedure
         */
        PROCEDURE
    }
}
