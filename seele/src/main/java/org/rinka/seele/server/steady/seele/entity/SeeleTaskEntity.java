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
 * Class : SeeleTaskEntity
 * Usage :
 */
@ToString
@Entity
@Table(name = "seele_task", schema = "seele_workflow")
public class SeeleTaskEntity {
    private long id;
    private String namespace;
    private long procedureId;
    private String name;
    private String skill;
    private String principle;
    private String documentation;
    private String arguments;
    private String eventCallbackMask;
    private String hooks;
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
    @Column(name = "namespace", nullable = false, length = 255)
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Basic
    @Column(name = "procedure_id", nullable = false)
    public long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(long procedureId) {
        this.procedureId = procedureId;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 511)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "skill", nullable = false, length = 2047)
    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    @Basic
    @Column(name = "principle", nullable = false, length = 2047)
    public String getPrinciple() {
        return principle;
    }

    public void setPrinciple(String principle) {
        this.principle = principle;
    }

    @Basic
    @Column(name = "documentation", nullable = false, length = -1)
    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
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
    @Column(name = "event_callback_mask", nullable = false, length = 2047)
    public String getEventCallbackMask() {
        return eventCallbackMask;
    }

    public void setEventCallbackMask(String eventCallbackMask) {
        this.eventCallbackMask = eventCallbackMask;
    }

    @Basic
    @Column(name = "hooks", nullable = false, length = -1)
    public String getHooks() {
        return hooks;
    }

    public void setHooks(String hooks) {
        this.hooks = hooks;
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
        SeeleTaskEntity that = (SeeleTaskEntity) o;
        return id == that.id &&
                procedureId == that.procedureId &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(name, that.name) &&
                Objects.equals(skill, that.skill) &&
                Objects.equals(principle, that.principle) &&
                Objects.equals(documentation, that.documentation) &&
                Objects.equals(arguments, that.arguments) &&
                Objects.equals(eventCallbackMask, that.eventCallbackMask) &&
                Objects.equals(hooks, that.hooks) &&
                Objects.equals(updatetime, that.updatetime) &&
                Objects.equals(newdate, that.newdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, namespace, procedureId, name, skill, principle, documentation, arguments, eventCallbackMask, hooks, updatetime, newdate);
    }
}
