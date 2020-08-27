/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.yurily.seele.server.connect.rest;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
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

    public void add(String supervisorId, String host, String callback, String fallback) {
        this.supervisors.put(supervisorId, new CallableSupervisor(supervisorId, host, callback, fallback));
    }

    public Map<String, CallableSupervisor> getAll() {
        return Collections.unmodifiableMap(this.supervisors);
    }

    public Optional<CallableSupervisor> get(String supervisorId) {
        CallableSupervisor cs = this.supervisors.get(supervisorId);
        if (cs == null) {
            log.warn("Try to callback a supervisor but not exist in pool: " + supervisorId);
        }
        return Optional.ofNullable(cs);
    }

    public Optional<CallableSupervisor> get() {
        if (this.supervisors.size() == 0) {
            return Optional.empty();
        }
        CallableSupervisor cs = this.supervisors.values().iterator().next();
        return Optional.ofNullable(cs);
    }

    public Optional<CallableSupervisor> remove(String supervisorId) {
        CallableSupervisor cs = this.supervisors.remove(supervisorId);
        if (cs == null) {
            log.warn("Try to callback a supervisor but not exist in pool: " + supervisorId);
        }
        return Optional.ofNullable(cs);
    }
}
