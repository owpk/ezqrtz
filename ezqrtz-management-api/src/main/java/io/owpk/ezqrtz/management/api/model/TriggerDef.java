package io.owpk.ezqrtz.management.api.model;

import lombok.Builder;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record TriggerDef(
        String id,
        String jobId,
        @Singular("jobParam")
        Map<String, Object> jobData,
        String name,
        String description,
        String cronExpression,
        TriggerState state,
        LocalDateTime nextFireTime,
        LocalDateTime previousFireTime
) {

    public TriggerDef {
        if (description == null)
            description = "";
        if (jobData == null)
            jobData = Map.of();
        if (state == null)
            state = TriggerState.NONE;
    }
}
