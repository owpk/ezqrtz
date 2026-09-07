package com.ocrv.helper.quartz.core.model;

import com.ocrv.helper.quartz.core.api.QuartzTriggerNamesapce;
import lombok.Builder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;

@Builder
public record SchedulerNamespace(
        String jobGroup,
        String triggerGroup
) implements QuartzTriggerNamesapce {

    public SchedulerNamespace() {
        this(Scheduler.DEFAULT_GROUP, Scheduler.DEFAULT_GROUP);
    }

    public JobKey jobKey(String identity) {
        return new JobKey(identity, jobGroup);
    }

    public TriggerKey triggerKey(String identity) {
        return new TriggerKey(identity, triggerGroup);
    }

}