/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/18
 */
package org.yurily.seele.server.steady.seele.entity;

import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Class : SeeleItemlogEntity
 * Usage :
 */
@ToString
@Entity
@Table(name = "seele_itemlog", schema = "seele_workflow")
public class SeeleItemlogEntity {
    private long zzid;
    private String wid;
    private boolean finished;
    private String content;
    private Timestamp newdate;
    private Timestamp updatetime;

    @Id
    @Column(name = "zzid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getZzid() {
        return zzid;
    }

    public void setZzid(long zzid) {
        this.zzid = zzid;
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
    @Column(name = "finished", nullable = false)
    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Basic
    @Column(name = "content", nullable = true, length = -1)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
        SeeleItemlogEntity that = (SeeleItemlogEntity) o;
        return zzid == that.zzid &&
                finished == that.finished &&
                Objects.equals(wid, that.wid) &&
                Objects.equals(content, that.content) &&
                Objects.equals(newdate, that.newdate) &&
                Objects.equals(updatetime, that.updatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zzid, wid, finished, content, newdate, updatetime);
    }
}
