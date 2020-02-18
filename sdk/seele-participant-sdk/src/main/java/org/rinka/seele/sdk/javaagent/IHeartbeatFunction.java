/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.rinka.seele.sdk.javaagent;

/**
 * Class : IHeartbeatFunction
 * Usage : 工作流Agent进行心跳动作的处理函数
 */
public interface IHeartbeatFunction {

    /**
     * 心跳包的准备函数
     *
     * @return 返回一个对象，该对象用于发送给资源服务引擎
     */
    Object beat();

    /**
     * 心跳包发送成功的ACK回调函数
     *
     * @param ackPayload 资源服务引擎ACK报文
     */
    default void beatAck(Object... ackPayload) {
    }
}
