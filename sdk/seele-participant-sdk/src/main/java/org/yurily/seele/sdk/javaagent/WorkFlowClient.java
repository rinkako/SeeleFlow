/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.yurily.seele.sdk.javaagent;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.socket.client.Ack;
import io.socket.client.Manager;
import io.socket.engineio.client.Transport;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.yurily.seele.sdk.javaagent.logger.WorkflowLoggerFactory;
import org.yurily.seele.sdk.javaagent.util.JsonUtil;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Class : WorkFlowClient
 * Usage : 工作流客户端
 */
@Slf4j
public class WorkFlowClient {

    private final WorkFlowClientContext context;

    private static final String RSSystemRequest = "__WF_RS_SYSTEM_EVT__";
    private static final String RSSystemResponse = "__WF_RS_SYSTEM_RESPONSE_EVT__";
    private static final String RSParticipantRequest = "__WF_RS_PARTICIPANT_EVT__";
    private static final String RSParticipantResponse = "__WF_RS_PARTICIPANT_RESPONSE_EVT__";
    private static final String RSEvent = "__WF_RS_DISPATCH_EVT__";
    private static final String RSRequireMeta = "__WF_RS_REQUIRE_META__";
    private static final String RSResponseMeta = "__WF_RS_RESPONSE_META__";
    private static final String HeartBeatEvent = "__WF_HEARTBEAT_CLIENT_EVT__";
    private static final String HeartBeatResponseEvent = "__WF_HEARTBEAT_RESPONSE_EVT__";
    private static final String RSParticipantLogging = "__WF_PARTICIPANT_LOGGING__";

    private static final String KW_ParticipantServiceState = "ParticipantServiceState";

    private int connectedRSCount = 0;
    private boolean initialized = false;

    private ExecutorService concurrentPool;

    /**
     * 将该客户端连接到资源服务引擎
     *
     * @param masterToken 连接到资源服务的权限校验token
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void connect(String masterToken) {
        if (!this.initialized) {
            // 注册logger
            WorkflowLoggerFactory.init(this);
            // 资源池
            if (this.context.maxConcurrentSize <= 0) {
                this.concurrentPool = Executors.newCachedThreadPool();
                log.info("Workflow async pool: CachedThreadPool");
            } else {
                this.concurrentPool = Executors.newFixedThreadPool(this.context.maxConcurrentSize);
                log.info("Workflow async pool: FixedThreadPool, size: " + this.context.maxConcurrentSize);
            }
            // 权限校验
            this.context.webSocket.io().on(Manager.EVENT_TRANSPORT, transArgs -> {
                Transport transport = (Transport) transArgs[0];
                transport.on(Transport.EVENT_REQUEST_HEADERS, reqArgs -> {
                    Map<String, List<String>> headers = (Map<String, List<String>>) reqArgs[0];
                    headers.put("token", Collections.singletonList(masterToken));
                    headers.put("namespace", Collections.singletonList(context.namespace));
                    headers.put("participant-id", Collections.singletonList(context.uniqueId));
                    headers.put("lang", Collections.singletonList("java"));
                });
            });
            // 心跳返回
            this.context.heartbeatResponseFunctions.forEach(rf ->
                    this.context.webSocket.on(WorkFlowClient.HeartBeatResponseEvent, rf::accept));
            this.context.webSocket.on(WorkFlowClient.HeartBeatResponseEvent,
                    args -> log.info(String.format("Heartbeat response from RS: %s", Arrays.toString(args))));
            // 资源服务的内部系统请求
            this.context.webSocket.on(WorkFlowClient.RSSystemRequest, systemPayload -> {
                try {
                    WorkflowSystemEvent systemEvent = WorkflowSystemEvent.of(systemPayload);
                    switch (systemEvent.getAction()) {
                        case WorkflowSystemEvent.Action_PauseNode:
                            this.context.workingState = WorkFlowClientContext.ParticipantServiceStateType.Paused;
                            break;
                        case WorkflowSystemEvent.Action_ResumeNode:
                            this.context.workingState = WorkFlowClientContext.ParticipantServiceStateType.Working;
                            break;
                        default:
                            log.error("RS passed a system request but not support: " + systemEvent.getAction());
                            break;
                    }
                    WorkflowSystemEventResponse resp = new WorkflowSystemEventResponse();
                    resp.setPayloadTerm(KW_ParticipantServiceState, this.context.workingState);
                    this.context.webSocket.emit(WorkFlowClient.RSSystemResponse, resp);
                } catch (Exception ex) {
                    log.error("RS passed a system request but exception occurred: " + ex.getMessage());
                }
            });
            // 第一次初始化信息请求
            this.context.webSocket.on(WorkFlowClient.RSRequireMeta, metaPayload -> {
                if (context.metadataFunction != null) {
                    MetadataPackage stdMp = new MetadataPackage();
                    stdMp.setCommunicationTypeName("WebSocket");
                    stdMp.setNamespace(this.context.namespace);
                    stdMp.setDisplayName(this.context.displayName);
                    stdMp.setParticipantId(this.context.uniqueId);
                    stdMp.setReentrantTypeName(WorkFlowClientContext.ReentrantType.NotReentrant.name());
                    stdMp.addSkills(this.context.serviceTags);
                    MetadataPackage beat = context.metadataFunction.report(stdMp);
                    if (beat != null) {
                        try {
                            context.webSocket.emit(WorkFlowClient.RSResponseMeta, JsonUtil.dumps(beat),
                                    (Ack) metaAckPayload -> context.metadataFunction.reportAck(metaAckPayload));
                        } catch (JsonProcessingException e) {
                            log.error("cannot jsonify heartbeat metadata: " + e.getMessage());
                        }
                    }
                }
            });
            // 资源服务请求到达
            this.context.webSocket.on(WorkFlowClient.RSEvent, workitemPayload -> {
                String hint = Arrays.toString(workitemPayload);
                log.info(String.format("Received RS event: %s", hint.length() <= 1000 ? hint :
                        (hint.substring(0, 1000) + String.format("...(%s bytes omitted)", hint.length() - 1000))));
                try {
                    // parse workitem mail
                    JSONObject mail = ((JSONObject) workitemPayload[0]);
                    String workitemType = mail.get("taskName").toString();
                    String nodeId = mail.get("nodeId").toString();
                    String namespace = mail.get("namespace").toString();
                    String workitemId = mail.get("workitemId").toString();
                    String requestId = mail.get("requestId").toString();
                    Map<String, Object> payload = JsonUtil.parse(mail.get("args").toString(), Map.class);
                    this.requestSeeleWorkitemAccepted(namespace, workitemId);
                    CompletableFuture cf = CompletableFuture.runAsync(() -> {
                        Workitem workitem;
                        try {
                            workitem = Workitem.of(namespace, nodeId, requestId, workitemId, workitemType, payload);
                            workitem.setState(Workitem.ResourcingStateType.ACCEPTED);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        Logger scopedLogger = workitem.getLogger();
                        try {
                            IWorkitemHandler handler = this.context.handlerContainer.get(workitemType);
                            if (handler == null) {
                                log.error(String.format("RS dispatch workitem with type `%s` but no handler available", workitemType));
                                this.requestSeeleChangeWorkitemToTarget(workitem, Workitem.ResourcingStateType.EXCEPTION);
                                return;
                            }
                            // 调用事件处理器
                            try {
                                this.requestSeeleChangeWorkitemToTarget(workitem, Workitem.ResourcingStateType.RUNNING);
                                handler.submit(workitem);
                                this.requestSeeleChangeWorkitemToTarget(workitem, Workitem.ResourcingStateType.COMPLETED);
                            } catch (Exception runnerEx) {
                                this.requestSeeleChangeWorkitemToTarget(workitem, Workitem.ResourcingStateType.EXCEPTION);
                                log.error("Workitem completed with exception, inner ex: " + runnerEx.getMessage());
                                scopedLogger.error("Exception occurred when handle workitem: " + runnerEx.getMessage());
                            }
                        } finally {
                            if (scopedLogger != null) {
                                scopedLogger.detachAndStopAllAppenders();
                                WorkflowLoggerFactory.removeLogger(namespace, workitemId);
                            }
                        }
                    }, this.concurrentPool)
                            .whenComplete((res, err) -> {
                                if (err != null) {
                                    log.error(String.format("Workitem complete with rt exception: %s", err.getMessage()));
                                } else {
                                    log.info(String.format("Workitem complete: %s", workitemId));
                                }
                            });
                } catch (Exception e) {
                    log.error(String.format("Cannot handle workitem from RS: %s", e.getMessage()));
                }
            });
            this.context.webSocket.on("connected", args -> log.info("RS connected in with her handshake: " + Arrays.toString(args)));
            this.initialized = true;
        }
        this.context.webSocket.connect();
    }

    /**
     * 断开与资源服务引擎的连接
     */
    public void disconnect() {
        this.context.webSocket.disconnect();
    }

    /**
     * 获取是否已经与资源服务引擎建立连接
     */
    public boolean connected() {
        return this.context.webSocket.connected();
    }

    /**
     * 进行一次心跳。
     * 这个方法不会周期性调用，需要由外部系统主动周期性地调度此方法。
     * 这是因为不是每一个系统都需要进行周期心跳上报数据。
     */
    public void beat() {
        this.context.heartbeatFunctions.forEach(hf -> {
            Object forSend = hf.beat();
            if (forSend != null) {
                this.context.webSocket.emit(WorkFlowClient.HeartBeatEvent, forSend, (Ack) hf::beatAck);
            }
        });
    }

    /**
     * 将格式化完毕的日志发送到工作流系统
     *
     * @param formattedLog 已经格式化完毕的
     */
    public void logToRS(String workitemId, String formattedLog, boolean bulk) {
        Map<String, String> logItem = new HashMap<>();
        logItem.put("bulk", String.valueOf(bulk));
        logItem.put("wid", workitemId);
        logItem.put("ts", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        logItem.put("log", formattedLog);
        try {
            this.context.webSocket.emit(WorkFlowClient.RSParticipantLogging, JsonUtil.dumps(logItem));
        } catch (JsonProcessingException e) {
            log.error("cannot jsonify workitem logging: " + e.toString());
        }
    }

    private void requestSeeleChangeWorkitemToTarget(Workitem workitem, Workitem.ResourcingStateType target) {
        ParticipantRequestMail prMail = new ParticipantRequestMail();
        prMail.setNamespace(workitem.getNamespace());
        prMail.setWorkitemId(workitem.getWid());
        prMail.setTargetState(target.name());
        try {
            String jStr = JsonUtil.dumps(prMail);
            this.context.webSocket.emit(WorkFlowClient.RSParticipantRequest, jStr);
        } catch (JsonProcessingException e) {
            log.error("cannot jsonify string: " + e.getMessage());
        }
        workitem.setState(target);
    }

    private void requestSeeleWorkitemAccepted(String namespace, String wid) {
        ParticipantRequestMail prMail = new ParticipantRequestMail();
        prMail.setNamespace(namespace);
        prMail.setWorkitemId(wid);
        prMail.setTargetState(Workitem.ResourcingStateType.ACCEPTED.name());
        try {
            String jStr = JsonUtil.dumps(prMail);
            this.context.webSocket.emit(WorkFlowClient.RSParticipantRequest, jStr);
        } catch (JsonProcessingException e) {
            log.error("cannot jsonify string: " + e.getMessage());
        }
    }

    /**
     * 新建一个工作流客户端
     *
     * @param context 客户端上下文，由{@link WorkFlowClientBuilder}构建
     */
    static WorkFlowClient of(WorkFlowClientContext context) {
        return new WorkFlowClient(context);
    }

    /**
     * 私有构造器避免外部构造
     */
    private WorkFlowClient(WorkFlowClientContext ctx) {
        this.context = ctx;
    }

    @Data
    @ToString
    @EqualsAndHashCode
    private static class RSLogEvent implements Serializable {
        private String namespace;

        private String workitemId;

        private String content;

        private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Data
    @ToString
    @EqualsAndHashCode
    private static class ParticipantRequestMail {
        private String namespace;

        private String workitemId;

        private String targetState;

        private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
