package io.owpk.ezqrtz.core.model;

import io.owpk.ezqrtz.core.api.CollisionStrategy;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ScheduleResult(
        boolean scheduled,
        Instant nextFireTime,
        CollisionStrategy appliedStrategy
) {
}