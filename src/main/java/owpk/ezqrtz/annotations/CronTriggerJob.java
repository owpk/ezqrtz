package owpk.ezqrtz.annotations;

import org.springframework.stereotype.Component;
import owpk.ezqrtz.internal.collision.CollisionStrategyType;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface CronTriggerJob {

    String cron();

    String description();

    String name();

    String group();

    CollisionStrategyType collisionStrategy() default CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS;

    String zoneId() default "";

    boolean enabled() default true;
}