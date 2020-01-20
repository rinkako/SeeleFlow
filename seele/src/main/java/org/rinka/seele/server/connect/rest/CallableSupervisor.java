/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.connect.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class : CallableSupervisor
 * Usage :
 */
@AllArgsConstructor
public class CallableSupervisor {

    @Getter
    private String supervisorId;

    @Getter
    private String host;

    @Getter
    private String callbackUri;

    @Getter
    private String fallbackUri;
}
