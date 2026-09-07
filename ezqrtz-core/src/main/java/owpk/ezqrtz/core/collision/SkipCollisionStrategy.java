package owpk.ezqrtz.core.collision;

import owpk.ezqrtz.core.api.CollisionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.util.function.Supplier;

@Slf4j
public final class SkipCollisionStrategy implements CollisionStrategy {

    @Override
    public boolean handle(
            Scheduler scheduler,
            JobKey jobKey,
            Supplier<JobDetail> jobDetailFactory,
            Supplier<Trigger> triggerFactory
    ) {
        log.debug("Job '{}' already exists. Scheduling skipped.", jobKey);
        return false;
    }

}