package owpk.ezqrtz.internal.collision;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import owpk.ezqrtz.api.CollisionStrategy;

import java.util.function.Supplier;

@Slf4j
public class RescheduleIfExistsStrategy implements CollisionStrategy {

    @Override
    public boolean handle(Scheduler scheduler, JobKey jobKey, Supplier<JobDetail> jobDetailFactory, Supplier<Trigger> triggerFactory) throws SchedulerException {
        var trigger = triggerFactory.get();
        var jobDetails = jobDetailFactory.get();
        var triggerKey = trigger.getKey();
        scheduler.addJob(jobDetails, true);
        scheduler.rescheduleJob(triggerKey, trigger);

        log.info("Trigger: {}, was successfully replaced and updated: {}", triggerKey, trigger);
        return false;
    }
}
