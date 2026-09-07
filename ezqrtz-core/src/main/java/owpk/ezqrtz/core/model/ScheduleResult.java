package owpk.ezqrtz.core.model;

import owpk.ezqrtz.core.api.CollisionStrategy;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ScheduleResult(
        boolean scheduled,
        Instant nextFireTime,
        CollisionStrategy appliedStrategy
) {
}