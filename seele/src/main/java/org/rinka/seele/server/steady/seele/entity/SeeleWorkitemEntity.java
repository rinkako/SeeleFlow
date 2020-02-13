/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.rinka.seele.server.steady.seele.entity;

import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Class : SeeleWorkitemEntity
 * Usage :
 */
@ToString
@Entity
@Table(name = "seele_workitem", schema = "seele_workflow")
public class SeeleWorkitemEntity {
    private long id;
    private String wid;
    private long taskId;
    private String taskName;
    private String requestId;
    private String queueId;
    private String arguments;
    private Timestamp createTime;
    private Timestamp enableTime;
    private Timestamp startTime;
    private Timestamp completeTime;
    private String state;
    private Timestamp updatetime;
    private Timestamp newdate;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "wid", nullable = false, length = 37)
    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    @Basic
    @Column(name = "task_id", nullable = false)
    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    @Basic
    @Column(name = "task_name", nullable = false)
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Basic
    @Column(name = "request_id", nullable = true, length = 37)
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Basic
    @Column(name = "queue_id", nullable = true)
    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    @Basic
    @Column(name = "arguments", nullable = false, length = -1)
    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    @Basic
    @Column(name = "create_time", nullable = true)
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "enable_time", nullable = true)
    public Timestamp getEnableTime() {
        return enableTime;
    }

    public void setEnableTime(Timestamp enableTime) {
        this.enableTime = enableTime;
    }

    @Basic
    @Column(name = "start_time", nullable = true)
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "complete_time", nullable = true)
    public Timestamp getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Timestamp completeTime) {
        this.completeTime = completeTime;
    }

    @Basic
    @Column(name = "state", nullable = false, length = 255)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Basic
    @Column(name = "updatetime", nullable = false, insertable = false, updatable = false)
    public Timestamp getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Timestamp updatetime) {
        this.updatetime = updatetime;
    }

    @Basic
    @Column(name = "newdate", nullable = false, insertable = false, updatable = false)
    public Timestamp getNewdate() {
        return newdate;
    }

    public void setNewdate(Timestamp newdate) {
        this.newdate = newdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeeleWorkitemEntity that = (SeeleWorkitemEntity) o;
        return id == that.id &&
                taskId == that.taskId &&
                Objects.equals(taskName, that.taskName) &&
                Objects.equals(queueId, that.queueId) &&
                Objects.equals(wid, that.wid) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(arguments, that.arguments) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(enableTime, that.enableTime) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(completeTime, that.completeTime) &&
                Objects.equals(state, that.state) &&
                Objects.equals(updatetime, that.updatetime) &&
                Objects.equals(newdate, that.newdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, wid, taskId, taskName, requestId, queueId, arguments, createTime, enableTime, startTime, completeTime, state, updatetime, newdate);
    }
}
