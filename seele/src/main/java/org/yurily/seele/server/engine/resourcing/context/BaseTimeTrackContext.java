/*
 * Author : Rinka
 * Date   : 2020/2/10
 */
package org.yurily.seele.server.engine.resourcing.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * Class : BaseTimeTrackContext
 * Usage :
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseTimeTrackContext extends BaseSeeleRSContext {

    private Timestamp updateTime;

    private Timestamp createTime;
}
