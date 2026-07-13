package owpk.ezqrtz.annotations;

import org.springframework.context.annotation.Import;
import owpk.ezqrtz.spring.config.QuartzConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vyacheslav Vorobev
 */
@Import({QuartzConfig.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableEzQuartzScheduler {
}