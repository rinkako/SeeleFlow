/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.rinka.seele.sdk.javaagent;

/**
 * Class : IWorkitemHandler
 * Usage : 可以处理supervisor下发的workitem的处理器公共接口
 */
public interface IWorkitemHandler {

    /**
     * 同步处理一个工作项
     *
     * @param workitem 工作项的数据包装，由workflow解析RS生成的supervisor给定的任务得到
     */
    void submit(Workitem workitem);
}
