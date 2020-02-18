/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.rinka.seele.sdk.javaagent.sdk.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.sdk.javaagent.sdk.WorkFlowClient;
import org.rinka.seele.sdk.javaagent.sdk.util.JsonUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class : SeeleWorkflowBulkAppender
 * Usage :
 */
@Slf4j
public class SeeleWorkflowBulkAppender extends ConsoleAppender<ILoggingEvent> {

    private final String workitemId;
    private final WorkFlowClient wfClient;
    private final int maxBulkSize;
    private final ConcurrentLinkedQueue<String> bufferQueue = new ConcurrentLinkedQueue<>();

    public SeeleWorkflowBulkAppender(String workitemId, WorkFlowClient wfClient) {
        this(workitemId, wfClient, 20);
    }

    public SeeleWorkflowBulkAppender(String workitemId, WorkFlowClient wfClient, int maxBulkSize) {
        this.workitemId = workitemId;
        this.wfClient = wfClient;
        this.maxBulkSize = maxBulkSize;
    }

    @Override
    public void append(ILoggingEvent event) {
        try {
            byte[] encodedBytes = this.encoder.encode(event);
            String formattedLog = new String(encodedBytes);
            this.bufferQueue.add(formattedLog);
            if (this.bufferQueue.size() >= this.maxBulkSize) {
                this.flushBulk();
            }

        } catch (Exception e) {
            log.error("Scoped logger exception occurred: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            log.info("stopping bulk appender, flush buffered log item: " + this.bufferQueue.size());
            this.flushBulk();
        } catch (Exception ee) {
            log.error("cannot elegantly stop Seele bulk appender: " + ee.getMessage());
        } finally {
            super.stop();
        }
    }

    private synchronized void flushBulk() {
        Object[] bulked = this.bufferQueue.toArray();
        try {
            String dumpBulk = JsonUtil.dumps(bulked);
            this.wfClient.logToRS(this.workitemId, dumpBulk, true);
        } catch (JsonProcessingException e) {
            log.error("cannot process workitem bulked log json dump: " + e.getMessage());
        } finally {
            this.bufferQueue.clear();
        }
    }
}
