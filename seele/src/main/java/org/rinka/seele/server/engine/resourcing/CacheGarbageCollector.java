/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/18
 */
package org.rinka.seele.server.engine.resourcing;

import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Class : CacheGarbageCollector
 * Usage :
 */
@Slf4j
@Component
@EnableScheduling
public class CacheGarbageCollector {

    @PostConstruct
    private void postConstruct() {

    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    private void workitemCacheCollect() {
        log.info("begin workitem cache GC");
        try {

        } catch (Exception ee) {
            log.error("workitem gc exception: " + ee.getMessage());
        }
        log.info("finish workitem cache GC");
    }

}
