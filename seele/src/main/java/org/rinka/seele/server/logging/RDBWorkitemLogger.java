/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/18
 */
package org.rinka.seele.server.logging;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class : RDBWorkitemLogger
 * Usage :
 */
public class RDBWorkitemLogger implements WorkitemLogger<RDBWorkitemLogger> {

    private ConcurrentLinkedQueue<String> buffer = new ConcurrentLinkedQueue<>();

    public RDBWorkitemLogger append(String logLine) {
        this.buffer.add(logLine);
        return this;
    }

    public String consumeOne() {
        return this.buffer.poll();
    }

    public String dumpMultilineString() {
        return this.dumpString("\n");
    }

    public String dumpString(String delimiter) {
        return String.join(delimiter, this.buffer);
    }

    public int size() {
        return this.buffer.size();
    }

    public void clear() {
        this.buffer.clear();
    }
}
