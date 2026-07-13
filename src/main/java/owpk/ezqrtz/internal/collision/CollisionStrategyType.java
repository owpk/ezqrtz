package owpk.ezqrtz.internal.collision;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import owpk.ezqrtz.api.CollisionStrategy;

@RequiredArgsConstructor
@Getter
public enum CollisionStrategyType {
    FAIL(new FailCollisionStrategy()),
    SKIP(new SkipCollisionStrategy()),
    REMOVE(new RemoveCollisionStrategy()),
    SKIP_AND_REPLACE_JOB_DATA(new SkipAndReplaceJobDataStrategy()),
    REPLACE_AND_RESCHEDULE_IF_EXISTS(new RescheduleIfExistsStrategy());

    private final CollisionStrategy strategy;
}
