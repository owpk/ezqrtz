package io.owpk.ezqrtz.core.model;

import io.owpk.ezqrtz.core.api.CollisionStrategy;
import io.owpk.ezqrtz.core.collision.CollisionStrategyType;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Objects;
import java.util.function.Consumer;

@Builder(toBuilder = true)
public record ScheduleRequest(
        Class<? extends Job> jobClass,
        String jobIdentity,
        String triggerIdentity,
        Boolean durable,
        boolean requestRecovery,
        CollisionStrategy collisionStrategy,
        Consumer<JobDataMap> jobDataCustomizer,
        Consumer<JobBuilder> jobCustomizer,
        Consumer<TriggerBuilder<Trigger>> triggerCustomizer,

        @Nullable String description,
        @Nullable TriggerDefinition trigger
) {

    public ScheduleRequest {
        if (Objects.isNull(durable))
            durable = true;
        if (Objects.isNull(jobClass))
            throw new IllegalStateException("Job class should not be null");
        if (Objects.isNull(jobIdentity) || Objects.isNull(triggerIdentity))
            throw new IllegalStateException("Job identity or trigger identity should not be null");
        if (Objects.isNull(collisionStrategy))
            collisionStrategy = CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS.getStrategy();
        if (Objects.isNull(jobDataCustomizer))
            jobDataCustomizer = _ -> {
            };
        if (Objects.isNull(jobCustomizer))
            jobCustomizer = _ -> {
            };
        if (Objects.isNull(triggerCustomizer))
            triggerCustomizer = _ -> {
            };
    }
}