package com.ocrv.helper.quartz.core.collision;

import com.ocrv.helper.quartz.core.api.CollisionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.function.Supplier;

@Slf4j
public class RescheduleIfExistsStrategy implements CollisionStrategy {

    @Override

    public boolean handle(
            Scheduler scheduler,
            JobKey jobKey,
            Supplier<JobDetail> jobDetailFactory,
            Supplier<Trigger> triggerFactory
    ) throws SchedulerException {

        var newJobDetail = jobDetailFactory.get();
        var newTrigger = triggerFactory.get();
        var triggerKey = newTrigger.getKey();

        var existingTrigger = scheduler.getTrigger(triggerKey);

        Trigger.TriggerState previousState = null;
        if (existingTrigger != null) {
            if (!existingTrigger.getJobKey().equals(jobKey)) {
                log.warn("Trigger '{}' is scheduled for another job '{}'. Skipping update.", existingTrigger.getJobKey(), jobKey);
                return false;
            }
            previousState = scheduler.getTriggerState(triggerKey);
        }

        scheduler.addJob(newJobDetail, true);
        var nextFireTime = scheduler.rescheduleJob(triggerKey, newTrigger);
        restoreTriggerState(scheduler, triggerKey, previousState);

        log.info(
                "Trigger '{}' successfully updated. Previous state: {}, next fire time: {}",
                triggerKey,
                previousState,
                nextFireTime
        );
        return false;
    }

    protected void restoreTriggerState(
            Scheduler scheduler,
            TriggerKey triggerKey,
            Trigger.TriggerState previousState
    ) throws SchedulerException {

        if (previousState == null) {
            return;
        }

        switch (previousState) {
            case PAUSED -> scheduler.pauseTrigger(triggerKey);
            case NORMAL, NONE -> {
                // nothing
            }
            case BLOCKED -> {
                // transient scheduler state, cannot be restored
            }
            case COMPLETE -> {
                // old trigger has completed, new trigger starts its own lifecycle
            }
            case ERROR -> log.warn(
                    "Previous trigger '{}' was in ERROR state. " +
                            "The new trigger will remain in NORMAL state.",
                    triggerKey
            );
        }
    }
}
