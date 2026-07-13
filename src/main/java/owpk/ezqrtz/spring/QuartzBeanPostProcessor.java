package owpk.ezqrtz.spring;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import owpk.ezqrtz.annotations.CronTriggerJob;
import owpk.ezqrtz.annotations.Execute;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;

public record QuartzBeanPostProcessor(EzQuartzJobRegistrar registrar) implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopUtils.getTargetClass(bean);

        CronTriggerJob ezQuartzJob = AnnotationUtils.findAnnotation(
                bean.getClass(),
                CronTriggerJob.class);

        if (ezQuartzJob == null)
            return bean;

        Method method = Arrays.stream(beanClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Execute.class))
                .findAny().orElseThrow(() -> new IllegalStateException("@Execute should be present on " + beanClass.getName()));

        try {
            method.setAccessible(true);
            MethodHandle methodHandle = MethodHandles.lookup()
                    .unreflect(method)
                    .bindTo(bean);
            registrar.register(bean, ezQuartzJob, methodHandle);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return bean;
    }
}