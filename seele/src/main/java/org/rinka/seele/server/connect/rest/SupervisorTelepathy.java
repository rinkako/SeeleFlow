/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.connect.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : SupervisorTelepathy
 * Usage :
 */
@Slf4j
@Component
public class SupervisorTelepathy {

    public static final String KEY_RESPONSE_CODE = "200";
    public static final String KEY_RESPONSE_MSG = "msg";

    @Autowired
    private RestTemplate rest;

    public void callback(CallableSupervisor supervisor, HashMap<String, String> payload) {
        if (supervisor == null) {
            log.error("Try to callback a supervisor but not exist.");
            return;
        }
        this.doCallback(supervisor, payload);
    }

    public void callback(String namespace, String supervisorId, HashMap<String, String> payload) {
        SupervisorRestPool.namespace(namespace).get(supervisorId).ifPresent(supervisor -> {
            doCallback(supervisor, payload);
        });
    }

    public void callback(String namespace, HashMap<String, String> payload) {
        SupervisorGroup sg = SupervisorRestPool.namespace(namespace);
        ConcurrentHashMap<String, CallableSupervisor> supervisors = sg.getSupervisors();
        if (supervisors.size() == 0) {
            log.error("callback without any supervisor: " + payload.toString());
            return;
        }
        CallableSupervisor cs = supervisors.values().iterator().next();
        doCallback(cs, payload);
    }

    private void doCallback(CallableSupervisor cs, HashMap<String, String> payload) {
        String cbUrl = cs.getCallbackUri();
        try {
            this.doPost(cbUrl);
        } catch (Exception ex) {
            log.warn("callback is fallback, retry to post");
            String fbUrl = cs.getFallbackUri();
            if (fbUrl != null) {
                try {
                    this.doPost(cbUrl);
                } catch (Exception fex) {
                    log.error("Try to fallback a supervisor but: " + fex.getMessage());
                }
            } else {
                log.error("Try fallback but no any candidate fallback-url, " + ex.getMessage());
            }
        }
    }

    private void doPost(String cbUrl) throws Exception {
        ResponseEntity<HashMap> resp = rest.postForEntity(cbUrl, null, HashMap.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("http rest status code not 200: " + resp.getStatusCode().toString());
        }
        HashMap respBody = resp.getBody();
        String bodyCode = respBody.getOrDefault(KEY_RESPONSE_CODE, "-1").toString();
        String msg = respBody.getOrDefault(KEY_RESPONSE_MSG, "NULL").toString();
        if (!bodyCode.equals("200")) {
            throw new Exception("body code not 200: " + bodyCode + " msg: " + msg);
        }
        log.info("Callback finished, resp: " + msg);
    }
}
