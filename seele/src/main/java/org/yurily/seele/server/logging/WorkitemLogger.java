/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/18
 */
package org.yurily.seele.server.logging;

public interface WorkitemLogger<_Ty> {

    _Ty append(String logLine);
}
