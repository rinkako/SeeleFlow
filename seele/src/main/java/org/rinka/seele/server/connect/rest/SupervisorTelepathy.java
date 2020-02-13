/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.connect.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.RSInteraction;
import org.rinka.seele.server.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : SupervisorTelepathy
 * Usage :
 */
@Slf4j
@Component
public class SupervisorTelepathy {

    public static final String KEY_RESPONSE_CODE = "code";
    public static final String KEY_RESPONSE_MSG = "msg";

    @Autowired
    private RestTemplate rest;

    public void callback(CallableSupervisor supervisor, @Valid RSInteraction.TransitionReply reply) {
        HashMap<String, Object> mapReply = JsonUtil.Mapper.convertValue(reply, new TypeReference<HashMap<String, Object>>() {});
        this.callback(supervisor, mapReply);
    }

    public void callback(CallableSupervisor supervisor, HashMap<String, Object> payload) {
        if (supervisor == null) {
            log.error("Try to callback a supervisor but not exist.");
            return;
        }
        this.doCallback(supervisor, payload);
    }

    public void callback(String namespace, String supervisorId, HashMap<String, Object> payload) {
        SupervisorRestPool.namespace(namespace).get(supervisorId).ifPresent(supervisor -> {
            doCallback(supervisor, payload);
        });
    }

    public void callback(String namespace, HashMap<String, Object> payload) {
        SupervisorGroup sg = SupervisorRestPool.namespace(namespace);
        ConcurrentHashMap<String, CallableSupervisor> supervisors = sg.getSupervisors();
        if (supervisors.size() == 0) {
            log.error("callback without any supervisor: " + payload.toString());
            return;
        }
        CallableSupervisor cs = supervisors.values().iterator().next();
        doCallback(cs, payload);
    }

    private void doCallback(CallableSupervisor cs, HashMap<String, Object> payload) {
        String host = cs.getHost();
        String cbUrl = cs.getCallbackUri();
        try {
            this.doPost(host + cbUrl, payload);
        } catch (Exception ex) {
            log.warn("callback is fallback, retry to post");
            String fbUrl = cs.getFallbackUri();
            if (fbUrl != null) {
                try {
                    this.doPost(fbUrl, payload);
                } catch (Exception fex) {
                    log.error("Try to fallback a supervisor but: " + fex.getMessage());
                }
            } else {
                log.error("Try fallback but no any candidate fallback-url, " + ex.getMessage());
            }
        }
    }

    private void doPost(String cbUrl, Map<String, Object> payload) throws Exception {
        ResponseEntity<String> resp = rest.postForEntity(cbUrl, payload, String.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("http rest status code not 200: " + resp.getStatusCode().toString());
        }
        String respBodyRaw = resp.getBody();
        Map respBody = JsonUtil.parse(respBodyRaw.toString(), Map.class);
        String bodyCode = respBody.getOrDefault(KEY_RESPONSE_CODE, "-1").toString();
        String msg = respBody.getOrDefault(KEY_RESPONSE_MSG, "NULL").toString();
        if (!bodyCode.equals("200")) {
            throw new Exception("body code not 200: " + bodyCode + " msg: " + msg);
        }
        log.info("Callback finished, resp: " + msg);
    }
}
