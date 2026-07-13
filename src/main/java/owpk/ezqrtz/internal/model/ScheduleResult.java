package owpk.ezqrtz.internal.model;

import lombok.Builder;
import owpk.ezqrtz.api.CollisionStrategy;

import java.time.Instant;

@Builder
public record ScheduleResult(
        boolean scheduled,
        Instant nextFireTime,
        CollisionStrategy appliedStrategy
) {
}