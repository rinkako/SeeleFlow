/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/4
 */
package org.yurily.seele.server;

import java.io.Serializable;
import java.util.UUID;

/**
 * Class : GDP
 * Usage : Global data package, contains global instance for common access.
 */
public class GDP implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SeeleId = String.format("Seele_%s", UUID.randomUUID().toString());
}
