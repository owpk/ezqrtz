package owpk.ezqrtz.core.collision;

import owpk.ezqrtz.core.api.CollisionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.util.function.Supplier;

@Slf4j
public class SkipAndReplaceJobDataStrategy implements CollisionStrategy {

    @Override
    public boolean handle(Scheduler scheduler, JobKey jobKey,
                          Supplier<JobDetail> jobDetailFactory,
                          Supplier<Trigger> triggerFactory
    ) {
        var jobDetail = jobDetailFactory.get();
        try {
            scheduler.addJob(jobDetail, true);
        } catch (Exception ex) {
            log.warn("Exception while replacing job data map: {}", ex.getLocalizedMessage());
        }
        return false;
    }
}
