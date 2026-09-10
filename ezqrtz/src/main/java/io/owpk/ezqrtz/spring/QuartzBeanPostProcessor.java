package io.owpk.ezqrtz.spring;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import io.owpk.ezqrtz.core.annotations.Execute;
import io.owpk.ezqrtz.core.annotations.EzCronJob;

public class QuartzBeanPostProcessor implements BeanPostProcessor {
    private final ObjectProvider<EzQuartzJobRegistrar> registrarProvider;

    public QuartzBeanPostProcessor(ObjectProvider<EzQuartzJobRegistrar> registrarProvider) {
        this.registrarProvider = registrarProvider;
    }

    public QuartzBeanPostProcessor(EzQuartzJobRegistrar registrar) {
        this(new ObjectProvider<>() {
            @Override
            public EzQuartzJobRegistrar getObject() {
                return registrar;
            }
        });
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopUtils.getTargetClass(bean);

        EzCronJob cztQuartzJob = AnnotationUtils.findAnnotation(
                bean.getClass(),
                EzCronJob.class);

        if (cztQuartzJob == null)
            return bean;

        Method method = Arrays.stream(beanClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Execute.class))
                .findAny().orElseThrow(() -> new IllegalStateException("@Execute should be present on " + beanClass.getName()));

        try {
            method.setAccessible(true);
            MethodHandle methodHandle = MethodHandles.lookup()
                    .unreflect(method)
                    .bindTo(bean);
                registrarProvider.getObject().register(bean, cztQuartzJob, methodHandle);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return bean;
    }
}