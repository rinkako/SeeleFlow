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
 * Class : SeeleSupervisorEntity
 * Usage :
 */
@ToString
@Entity
@Table(name = "seele_supervisor", schema = "seele_workflow")
public class SeeleSupervisorEntity {
    private long id;
    private String namespace;
    private String supervisorId;
    private String host;
    private String callbackUri;
    private String fallbackHost;
    private Timestamp newdate;
    private Timestamp updatetime;

    @Id
    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "namespace", nullable = false, length = 255)
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Basic
    @Column(name = "supervisor_id", nullable = false, length = 511)
    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    @Basic
    @Column(name = "host", nullable = false, length = 2047)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Basic
    @Column(name = "callback_uri", nullable = false, length = 2047)
    public String getCallbackUri() {
        return callbackUri;
    }

    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }

    @Basic
    @Column(name = "fallback_host", nullable = true, length = 2047)
    public String getFallbackHost() {
        return fallbackHost;
    }

    public void setFallbackHost(String fallbackHost) {
        this.fallbackHost = fallbackHost;
    }

    @Basic
    @Column(name = "newdate", nullable = false, insertable = false, updatable = false)
    public Timestamp getNewdate() {
        return newdate;
    }

    public void setNewdate(Timestamp newdate) {
        this.newdate = newdate;
    }

    @Basic
    @Column(name = "updatetime", nullable = false, insertable = false, updatable = false)
    public Timestamp getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Timestamp updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeeleSupervisorEntity that = (SeeleSupervisorEntity) o;
        return id == that.id &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(supervisorId, that.supervisorId) &&
                Objects.equals(host, that.host) &&
                Objects.equals(callbackUri, that.callbackUri) &&
                Objects.equals(fallbackHost, that.fallbackHost) &&
                Objects.equals(newdate, that.newdate) &&
                Objects.equals(updatetime, that.updatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, namespace, supervisorId, host, callbackUri, fallbackHost, newdate, updatetime);
    }
}
