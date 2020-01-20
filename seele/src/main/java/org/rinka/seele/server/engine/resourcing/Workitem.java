/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.rinka.seele.server.engine.resourcing;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class : Workitem
 * Usage :
 */
@Slf4j
public class Workitem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private String namespace;

    @Getter
    private String wid;

    @Getter
    private String skill;

    @Getter
    private HashMap<String, Object> args;

    // TODO
    @Getter
    private Object entity;

    // TODO
    @Getter
    private Object process;

    public static Workitem of(String namespace, String wid, boolean forceReload) {
        // TODO
        return null;
    }
}
