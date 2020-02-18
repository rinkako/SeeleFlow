/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/18
 */
package org.rinka.seele.server.engine.resourcing;

import lombok.extern.slf4j.Slf4j;
import org.rinka.seele.server.engine.resourcing.context.WorkitemContext;
import org.rinka.seele.server.logging.RDBWorkitemLogger;
import org.rinka.seele.server.steady.seele.entity.SeeleItemlogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class : CacheGarbageCollector
 * Usage :
 */
@Slf4j
@Component
@EnableScheduling
public class CacheGarbageCollector {

    @Autowired
    private RSInteraction interaction;

    @PostConstruct
    private void postConstruct() {

    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    private void workitemCacheCollect() {
        log.info("begin workitem cache GC");
        try {
            Collection<WorkitemContext> caches = WorkitemContext.WorkitemPool.values();
            Set<WorkitemContext> removeSet = new HashSet<>();
            for (WorkitemContext workitem : caches) {
                if (workitem.isLogArrived() && workitem.isFinalState() && !workitem.isLogFlushed()) {
                    workitem.markLogAlreadyFlushed();
                    try {
                        this.interaction.flushLogItem(workitem);
                        removeSet.add(workitem);
                    } catch (Exception ee) {
                        log.error("flush log for workitem fault, reset flush flag: " + ee.getMessage());
                        workitem.markLogNotFlush();
                    }
                }
            }
            for (WorkitemContext workitem : removeSet) {
                workitem.removeSelfFromCache();
                log.info(String.format("remove final state workitem[%s] %s (%s)", workitem.getWid(), workitem.getTaskName(), workitem.getState().name()));
            }
            log.info("finish workitem GC: " + removeSet.size());
        } catch (Exception ee) {
            log.error("workitem gc exception: " + ee.getMessage());
        }
    }

}
