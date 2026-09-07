package com.ocrv.helper.quartz.core.annotations;

import com.ocrv.helper.quartz.core.collision.CollisionStrategyType;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface CztCronJob {

    String cron();

    String description();

    String name();

    String group();

    CollisionStrategyType collisionStrategy() default CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS;

    String zoneId() default "";

    boolean enabled() default true;
}