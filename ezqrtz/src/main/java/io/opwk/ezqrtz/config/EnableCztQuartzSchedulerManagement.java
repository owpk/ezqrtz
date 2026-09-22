package io.opwk.ezqrtz.config;

import io.owpk.ezqrtz.management.adapter.InboundManagementController;
import io.owpk.ezqrtz.management.adapter.SchedulerManagementAdvice;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vyacheslav Vorobev
 */
@Import({
        InboundManagementController.class,
        QuartzManagementConfig.class,
        SchedulerManagementAdvice.class
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableCztQuartzSchedulerManagement {
}
