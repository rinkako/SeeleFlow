/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.yurily.seele.server.engine.resourcing.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Class : RSContext
 * Usage : resourcing service context
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class RSContext extends BaseTimeTrackContext {
    private String namespace;

}
