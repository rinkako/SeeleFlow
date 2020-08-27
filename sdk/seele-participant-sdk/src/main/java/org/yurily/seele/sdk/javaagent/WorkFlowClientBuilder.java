/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.yurily.seele.sdk.javaagent;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Class : WorkFlowClientBuilder
 * Usage : 工作流客户端构造器，此构造器不是线程安全的
 */
public class WorkFlowClientBuilder {

    /**
     * 构建中的上下文对象，最终用来生成{@link WorkFlowClient}
     */
    WorkFlowClientContext context;

    /**
     * 设置工作流引擎的连接地址
     *
     * @param rsEngineHosts 一个集合，包含所有候选的工作流引擎的连接地址
     */
    public SocketWorkFlowClientBuilder connectTo(Collection<String> rsEngineHosts) throws Exception {
        if (this.context.resourceServiceHosts == null) {
            this.context.resourceServiceHosts = new HashSet<>();
        }
        this.context.resourceServiceHosts.addAll(rsEngineHosts);
        return SocketWorkFlowClientBuilder.of(this);
    }

    /**
     * 设置本客户端的角色为Supervisor
     */
    public WorkFlowClientBuilder asSupervisor() throws Exception {
        if (this.context.participantType == null) {
            this.context.participantType = WorkFlowClientContext.ParticipantType.Supervisor;
        } else {
            throw new Exception("A workflow client type can only be set once");
        }
        return this;
    }

    /**
     * 设置本客户端的角色为Agent
     *
     * @param reentrantType 设置agent的全局可重入属性，可重入性质{@link WorkFlowClientContext.ReentrantType}
     */
    public WorkFlowClientBuilder asAgent(WorkFlowClientContext.ReentrantType reentrantType) throws Exception {
        if (this.context.participantType == null) {
            this.context.participantType = WorkFlowClientContext.ParticipantType.Agent;
            //Must.NotNull(reentrantType, "reentrantType of agent resource must not null");
            this.context.reentrantType = reentrantType;
        } else {
            throw new Exception("A workflow client type can only be set once");
        }
        return this;
    }

    /**
     * 设置本客户端的角色为人力资源
     */
    public WorkFlowClientBuilder asHuman() throws Exception {
        if (this.context.participantType == null) {
            this.context.participantType = WorkFlowClientContext.ParticipantType.Human;
        } else {
            throw new Exception("A workflow client type can only be set once");
        }
        return this;
    }

    /**
     * 添加该资源的服务标签
     *
     * @param serviceTags 一个集合，包含可以提供的服务类型
     */
    public WorkFlowClientBuilder addTag(Collection<String> serviceTags) {
        if (this.context.serviceTags == null) {
            this.context.serviceTags = new HashSet<>();
        }
        this.context.serviceTags.addAll(serviceTags);
        return this;
    }

    /**
     * 添加该资源的服务标签
     *
     * @param serviceTag 可以提供的服务类型
     */
    public WorkFlowClientBuilder addTag(String serviceTag) {
        if (this.context.serviceTags == null) {
            this.context.serviceTags = new HashSet<>();
        }
        this.context.serviceTags.add(serviceTag);
        return this;
    }

    /**
     * 开始创建一个新的工作流客户端
     *
     * @param namespace   命名空间
     * @param uniqueId    资源唯一id
     * @param displayName 用户可读名字
     * @param maxConcurrentSize 最大并发处理工作项的数量，0为无并发控制
     */
    public static WorkFlowClientBuilder create(String namespace, String uniqueId, String displayName, int maxConcurrentSize) {
        return new WorkFlowClientBuilder(namespace, uniqueId, displayName, maxConcurrentSize);
    }

    /**
     * 私有构造避免外部构造
     */
    private WorkFlowClientBuilder(String namespace, String uuid, String displayName, int maxConcurrentSize) {
        this.context = new WorkFlowClientContext(namespace, uuid, displayName, maxConcurrentSize);
    }

    /**
     * 带WS连接的工作流客户端构造器
     */
    public static class SocketWorkFlowClientBuilder {
        /**
         * 上级构造器
         */
        private final WorkFlowClientBuilder parent;

        /**
         * 新建一个新的带WS连接的工作流客户端构造器
         *
         * @param builder 上级构造器
         */
        static SocketWorkFlowClientBuilder of(WorkFlowClientBuilder builder) throws Exception {
            // TODO: 单点
            String chosenUrl = builder.context.resourceServiceHosts.iterator().next();
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.transports = new String[]{WebSocket.NAME, Polling.NAME};
            builder.context.webSocket = IO.socket(chosenUrl, options);
            return new SocketWorkFlowClientBuilder(builder);
        }

        /**
         * 私有构造器避免外部构造
         */
        private SocketWorkFlowClientBuilder(WorkFlowClientBuilder builder) {
            this.parent = builder;
        }

        /**
         * 设置工作项到达此客户端后执行何种处理函数
         *
         * @param event   工作项类型名
         * @param handler 处理函数
         */
        public SocketWorkFlowClientBuilder onWorkitem(String event, IWorkitemHandler handler) {
            this.parent.context.registerWorkitemHandler(event, handler);
            return this;
        }

        /**
         * 设置与资源服务引擎的连接生命周期事件执行何种处理函数
         *
         * @param socketEvent 工作项类型名
         * @param handler     处理函数
         */
        public SocketWorkFlowClientBuilder onConnection(String socketEvent, Emitter.Listener handler) {
            this.parent.context.webSocket.on(socketEvent, handler);
            return this;
        }

        /**
         * 设置被资源服务要求返回自身元数据信息时的处理函数
         *
         * @param handler 处理函数
         */
        public SocketWorkFlowClientBuilder onRequireMetadata(IMetadataReportFunction handler) {
            this.parent.context.metadataFunction = handler;
            return this;
        }

        /**
         * 增加一个与资源服务引擎心跳上报数据响应的处理函数
         *
         * @param handler 处理函数
         */
        public SocketWorkFlowClientBuilder addHeartbeatResponseHandler(IHeartbeatResponseFunction handler) {
            this.parent.context.heartbeatResponseFunctions.add(handler);
            return this;
        }

        /**
         * 增加一个与资源服务引擎心跳上报数据的准备函数
         *
         * @param handler 处理函数
         */
        public SocketWorkFlowClientBuilder addHeartbeatHandler(IHeartbeatFunction handler) {
            this.parent.context.heartbeatFunctions.add(handler);
            return this;
        }

        /**
         * 构建一个工作流客户端连接对象
         */
        @SuppressWarnings("unchecked")
        public WorkFlowClient build() {
//            Must.NotNull(this.parent.context.resourceServiceHosts, "Workflow server host must be given at least one");
//            Must.NotNull(this.parent.context.participantType, "Resource type must be given");
//            Must.NotNull(this.parent.context.metadataFunction, "Metadata function type must be given");
            if (this.parent.context.serviceTags == null) {
                this.parent.context.serviceTags = Collections.EMPTY_SET;
            }
            return WorkFlowClient.of(this.parent.context);
        }
    }
}
