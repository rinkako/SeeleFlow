/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.rinka.seele.server.engine.resourcing.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.rinka.seele.server.engine.resourcing.principle.Principle;

import java.util.Map;

/**
 * Class : TaskContext
 * Usage :
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskContext extends RSContext {
    private String requestId;

    private String supervisorId;

    private ResourcingTaskSubmitType submitType;

    private String taskName;

    private Map<String, Object> args;

    private Principle principle;

    private String skill;

    public enum ResourcingTaskSubmitType {
        /**
         * directly submit by supervisor without any belonging active procedure
         */
        DIRECT,

        /**
         * submit by engine internally with a belonging active procedure
         */
        PROCEDURE
    }
}
