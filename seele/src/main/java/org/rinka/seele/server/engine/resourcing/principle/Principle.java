/*
 * Author : Rinka
 * Date   : 2020/2/10
 */
package org.rinka.seele.server.engine.resourcing.principle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.api.form.PrincipleForm;
import org.rinka.seele.server.util.JsonUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * Class : Principle
 * Usage :
 */
@Slf4j
@Data
@ToString
@EqualsAndHashCode
public class Principle implements Serializable {
    private static final long serialVersionUID = 1L;

    private DispatchType dispatchType;

    private String dispatcherName;

    private Map<String, Object> dispatcherArgs;

    public static Principle of(PrincipleForm descriptor) {
        Principle pp = new Principle();
        try {
            pp.setDispatcherName(descriptor.getDispatcherName());
            pp.setDispatchType(Enum.valueOf(DispatchType.class, descriptor.getDispatchType().toUpperCase()));
            pp.setDispatcherArgs(pp.getDispatcherArgs());
            return pp;
        }
        catch (Exception ex) {
            log.error("cannot parse principle: " + ex.getMessage());
            return null;
        }
    }

    public enum DispatchType {
        /**
         * Seele will choose a set of participants to allocate workitem
         */
        ALLOCATE,

        /**
         * Seele will prepare the workitem, and ask which participant want to handle it
         */
        OFFER
    }
}
