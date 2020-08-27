/*
 * Author : Rinka
 * Date   : 2020/2/10
 */
package org.yurily.seele.server.engine.resourcing.principle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.api.form.PrincipleForm;
import org.yurily.seele.server.util.JsonUtil;

import java.io.Serializable;
import java.util.HashMap;
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

    public static Principle of(String descriptor) {
        try {
            return JsonUtil.parseRaw(descriptor, new TypeReference<Principle>() {});
        } catch (JsonProcessingException e) {
            log.error("cannot parse principle: " + e.getMessage());
            return null;
        }
    }

    public String getDescriptor() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("dispatchType", this.dispatchType.name());
            map.put("dispatcherName", this.dispatcherName);
            map.put("dispatcherArgs", JsonUtil.dumps(this.dispatcherArgs));
            return JsonUtil.dumps(map);
        } catch (JsonProcessingException e) {
            log.error("cannot dump json: " + e.getMessage());
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
