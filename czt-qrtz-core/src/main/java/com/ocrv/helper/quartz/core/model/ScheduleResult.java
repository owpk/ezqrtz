package com.ocrv.helper.quartz.core.model;

import com.ocrv.helper.quartz.core.api.CollisionStrategy;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ScheduleResult(
        boolean scheduled,
        Instant nextFireTime,
        CollisionStrategy appliedStrategy
) {
}