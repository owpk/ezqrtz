package io.owpk.ezqrtz.core.collision;

import io.owpk.ezqrtz.core.api.CollisionStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CollisionStrategyType {
    FAIL(new FailCollisionStrategy()),
    DO_NOTHING(new SkipCollisionStrategy()),
    REMOVE(new RemoveCollisionStrategy()),
    REPLACE_JOB_DATA_AND_SKIP(new SkipAndReplaceJobDataStrategy()),
    REPLACE_AND_RESCHEDULE_IF_EXISTS(new RescheduleIfExistsStrategy());

    private final CollisionStrategy strategy;
}
