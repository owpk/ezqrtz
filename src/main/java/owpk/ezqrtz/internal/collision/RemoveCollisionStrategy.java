package owpk.ezqrtz.internal.collision;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import owpk.ezqrtz.api.CollisionStrategy;

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