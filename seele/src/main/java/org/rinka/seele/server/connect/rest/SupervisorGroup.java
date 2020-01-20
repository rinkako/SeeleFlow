/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.connect.rest;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : SupervisorGroup
 * Usage :
 */
@Slf4j
public class SupervisorGroup {

    public static final String KEY_HOST = "host";
    public static final String KEY_CALLBACK = "callback";
    public static final String KEY_FALLBACK = "fallback";

    @Getter
    private String namespace;

    @Getter
    private ConcurrentHashMap<String, CallableSupervisor> supervisors = new ConcurrentHashMap<>();

    public SupervisorGroup(String namespace) {
        this.namespace = namespace;
    }

    public void add(String supervisorId, HashMap<String, String> descriptor) {
        this.supervisors.put(supervisorId, new CallableSupervisor(supervisorId,
                descriptor.get(KEY_HOST), descriptor.get(KEY_CALLBACK), descriptor.get(KEY_FALLBACK)));
    }

    public Optional<CallableSupervisor> get(String supervisorId) {
        CallableSupervisor cs = this.supervisors.get(supervisorId);
        if (cs == null) {
            log.error("Try to callback a supervisor but not exist in pool: " + supervisorId);
        }
        return Optional.ofNullable(cs);
    }
}
