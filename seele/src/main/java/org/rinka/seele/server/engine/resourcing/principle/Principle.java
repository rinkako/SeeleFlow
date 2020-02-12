/*
 * Author : Rinka
 * Date   : 2020/2/10
 */
package org.rinka.seele.server.engine.resourcing.principle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
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

    @SuppressWarnings("all")
    public static Principle of(String descriptor) {
        Principle pp = new Principle();
        try {
            Map raw = JsonUtil.parse(descriptor, Map.class);
            pp.setDispatcherName((String) raw.get("dispatcherName"));
            pp.setDispatchType(Enum.valueOf(DispatchType.class, ((String) raw.get("dispatchType")).toUpperCase()));
            pp.setDispatcherArgs((Map<String, Object>) raw.get("dispatcherArgs"));
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
