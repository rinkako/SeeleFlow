/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.yurily.seele.sdk.javaagent;

/**
 * Class : IHeartbeatResponseFunction
 * Usage : 工作流Agent进行心跳动作后工作流引擎返回响应的处理函数
 */
public interface IHeartbeatResponseFunction {

    /**
     * 处理来自工作流引擎的响应
     *
     * @param response 来自资源服务引擎的响应报文段
     */
    void accept(Object... response);
}
