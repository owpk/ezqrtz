package owpk.ezqrtz.internal.collision;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import owpk.ezqrtz.api.CollisionStrategy;
import owpk.ezqrtz.exception.JobCollisionException;

import java.util.function.Supplier;

public final class FailCollisionStrategy implements CollisionStrategy {

    @Override
    public boolean handle(
            Scheduler scheduler,
            JobKey jobKey,
            Supplier<JobDetail> jobDetailFactory,
            Supplier<Trigger> triggerFactory
    ) {
        throw new JobCollisionException(
                "Job '%s' already exists".formatted(jobKey)
        );
    }

}