package com.ocrv.helper.quartz.management.api.model;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Builder
public record TriggerFilter(
                            @Nullable String id,
                            @Nullable String group,
                            @Nullable String name,
                            @Nullable String description,
                            @Nullable String cronExpression,
                            @Nullable LocalDateTime nextFireTimeFrom,
                            @Nullable LocalDateTime nextFireTimeTo) {

    public static TriggerFilter empty() {
        return TriggerFilter.builder().build();
    }

    public boolean hasId() {
        return  id != null && !id.isBlank();
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public boolean hasGroup() {
        return group != null && !group.isBlank();
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasCronExpression() {
        return cronExpression != null && !cronExpression.isBlank();
    }

    public boolean hasNextFireTimeFrom() {
        return nextFireTimeFrom != null;
    }

    public boolean hasNextFireTimeTo() {
        return nextFireTimeTo != null;
    }

    public boolean isEmpty() {
        return !hasId() && !hasDescription() && !hasGroup() && !hasName() && !hasCronExpression() &&
                !hasNextFireTimeFrom() && !hasNextFireTimeTo();
    }
}
