/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.yurily.seele.sdk.javaagent.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.sdk.javaagent.WorkFlowClient;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : WorkflowLogger
 * Usage :
 */
@Slf4j
public class WorkflowLoggerFactory {

    public static final ConcurrentHashMap<String, Logger> loggerCache = new ConcurrentHashMap<>();

    private static boolean initialized = false;
    private static WorkFlowClient wfClient;

    public static void init(WorkFlowClient wfClient) {
        WorkflowLoggerFactory.wfClient = wfClient;
        WorkflowLoggerFactory.initialized = true;
    }

    public static Logger getLogger(String namespace, String workitemId) throws Exception {
        if (!WorkflowLoggerFactory.initialized) {
            throw new Exception("get workitem scoped logger without init factory");
        }
        return WorkflowLoggerFactory.getLogger(namespace, workitemId, WorkflowLoggerFactory.wfClient);
    }

    public static Logger getLogger(String namespace, String workitemId, WorkFlowClient wfClient) {
        String loggerName = WorkflowLoggerFactory.generateLoggerName(namespace, workitemId);
        return WorkflowLoggerFactory.loggerCache
                .computeIfAbsent(loggerName, lg -> WorkflowLoggerFactory.createLogger(namespace, workitemId, wfClient, false));
    }

    public static Logger getBulkLogger(String namespace, String workitemId) throws Exception {
        if (!WorkflowLoggerFactory.initialized) {
            throw new Exception("get workitem scoped bulk logger without init factory");
        }
        return WorkflowLoggerFactory.getBulkLogger(namespace, workitemId, WorkflowLoggerFactory.wfClient);
    }

    public static Logger getBulkLogger(String namespace, String workitemId, WorkFlowClient wfClient) {
        String loggerName = WorkflowLoggerFactory.generateLoggerName(namespace, workitemId);
        return WorkflowLoggerFactory.loggerCache
                .computeIfAbsent(loggerName, lg -> WorkflowLoggerFactory.createLogger(namespace, workitemId, wfClient, true));
    }

    public static void removeLogger(String namespace, String workitemId) {
        String loggerName = WorkflowLoggerFactory.generateLoggerName(namespace, workitemId);
        Logger tbr = WorkflowLoggerFactory.loggerCache.remove(loggerName);
        tbr.detachAndStopAllAppenders();
    }

    private static String generateLoggerName(String namespace, String workitemId) {
        return String.format("%s:%s", namespace, workitemId);
    }

    private static Logger createLogger(String namespace, String workitemId, WorkFlowClient wfClient, boolean bulk) {
        String loggerName = WorkflowLoggerFactory.generateLoggerName(namespace, workitemId);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(loggerName);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.setPattern("%date{\"yyyy-MM-dd'T'HH:mm:ss.SSSXXX\", Asia/Shanghai} %-5level [%thread] %logger{50} [%file:%line] - %msg %n");
        encoder.start();

        OutputStreamAppender<ILoggingEvent> appender;
        if (bulk) {
            appender = new SeeleWorkflowBulkAppender(workitemId, wfClient);
        } else {
            appender = new SeeleWorkflowAppender(workitemId, wfClient);
        }
        appender.setName(loggerName);
        appender.setEncoder(encoder);
        appender.setContext(loggerContext);
        appender.start();

        logger.addAppender(appender);
        logger.setAdditive(false);
        logger.setLevel(Level.DEBUG);

        return logger;
    }
}
