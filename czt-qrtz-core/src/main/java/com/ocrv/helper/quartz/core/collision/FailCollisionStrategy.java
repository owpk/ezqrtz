package com.ocrv.helper.quartz.core.collision;

import com.ocrv.helper.quartz.core.api.CollisionStrategy;
import com.ocrv.helper.quartz.core.exception.JobCollisionException;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;

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