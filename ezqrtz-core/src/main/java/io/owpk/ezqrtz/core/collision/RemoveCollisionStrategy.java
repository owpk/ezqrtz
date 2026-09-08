package io.owpk.ezqrtz.core.collision;

import io.owpk.ezqrtz.core.api.CollisionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.function.Supplier;

@Slf4j
public final class RemoveCollisionStrategy implements CollisionStrategy {

    @Override
    public boolean handle(
            Scheduler scheduler,
            JobKey jobKey,
            Supplier<JobDetail> jobDetailFactory,
            Supplier<Trigger> triggerFactory
    ) throws SchedulerException {
        scheduler.deleteJob(jobKey);
        log.debug("Job '{}' was replaced", jobKey);
        return true;
    }

}