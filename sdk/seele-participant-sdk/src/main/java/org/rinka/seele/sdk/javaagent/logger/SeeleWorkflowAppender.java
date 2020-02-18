/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.rinka.seele.sdk.javaagent.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.sdk.javaagent.WorkFlowClient;

/**
 * Class : SeeleWorkflowAppender
 * Usage :
 */
@Slf4j
public class SeeleWorkflowAppender extends ConsoleAppender<ILoggingEvent> {

    private final String workitemId;
    private final WorkFlowClient wfClient;

    public SeeleWorkflowAppender(String workitemId, WorkFlowClient wfClient) {
        this.workitemId = workitemId;
        this.wfClient = wfClient;
    }

    @Override
    public void append(ILoggingEvent event) {
        try {
            byte[] encodedBytes = this.encoder.encode(event);
            String formattedLog = new String(encodedBytes);
            this.wfClient.logToRS(this.workitemId, formattedLog, false);
        } catch (Exception e) {
            log.error("Scoped logger exception occurred: " + e.getMessage());
        }
    }
}
