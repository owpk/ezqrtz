package owpk.ezqrtz.spring.config;

import owpk.ezqrtz.management.core.V1SchedulingManagementController;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vyacheslav Vorobev
 */
@Import({
        V1SchedulingManagementController.class,
        QuartzManagementConfig.class
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableCztQuartzSchedulerManagement {
}
