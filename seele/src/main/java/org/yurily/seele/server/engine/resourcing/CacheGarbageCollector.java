/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/18
 */
package org.yurily.seele.server.engine.resourcing;

import lombok.extern.slf4j.Slf4j;
import org.yurily.seele.server.engine.resourcing.context.WorkitemContext;
import org.yurily.seele.server.engine.resourcing.transition.WorkitemTransitionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
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

    private static final long GC_WAIT_TIME = 120_000L;

    @Autowired
    private RSInteraction interaction;

    @Autowired
    private WorkitemTransitionExecutor transitionExecutor;

    @PostConstruct
    private void postConstruct() {

    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 30_000)
    private void workitemCacheCollect() {
        log.info("begin workitem cache GC");
        int markGarbageCount = 0, collectGarbageCount = 0, flushLogCount = 0;
        long currentTs = System.currentTimeMillis();
        try {
            Collection<WorkitemContext> caches = WorkitemContext.WorkitemPool.values();
            Set<WorkitemContext> removeSet = new HashSet<>();
            // flush log
            for (WorkitemContext workitem : caches) {
                if (workitem.isLogArrived() && workitem.isFinalState()) {
                    removeSet.add(workitem);
                    if (!workitem.isLogFlushed()) {
                        workitem.markLogAlreadyFlushed();
                        try {
                            this.interaction.flushLogItem(workitem);
                            flushLogCount++;
                        } catch (Exception ee) {
                            log.error("flush log for workitem fault, reset flush flag: " + ee.getMessage());
                            workitem.markLogNotFlush();
                        }
                    }
                }
            }
            // remove cache
            for (WorkitemContext workitem : removeSet) {
                Timestamp gcTime = workitem.getMarkAsGarbageTime();
                if (gcTime == null) {
                    workitem.setMarkAsGarbageTime(Timestamp.from(ZonedDateTime.now().toInstant()));
                    markGarbageCount++;
                    log.info(String.format("Marked final state workitem[%s] %s (%s) to be garbage, it will be collected after %sms",
                            workitem.getWid(), workitem.getTaskName(), workitem.getState().name(), CacheGarbageCollector.GC_WAIT_TIME));
                    continue;
                }
                if (currentTs - gcTime.getTime() > CacheGarbageCollector.GC_WAIT_TIME) {
                    workitem.removeSelfFromCache();
                    transitionExecutor.removeTracker(workitem);
                    collectGarbageCount++;
                    log.info(String.format("Remove final state workitem[%s] %s (%s)", workitem.getWid(), workitem.getTaskName(), workitem.getState().name()));
                }
            }
            log.info(String.format("GC report: Collect[%s] Mark[%s] LogFlush[%s] Caching[%s]", collectGarbageCount, markGarbageCount, flushLogCount, WorkitemContext.WorkitemPool.size()));
        } catch (Exception ee) {
            log.error("Workitem gc exception: " + ee.getMessage());
        }
    }

}
