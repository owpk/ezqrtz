package owpk.ezqrtz.spring;

import owpk.ezqrtz.core.annotations.CztCronJob;
import owpk.ezqrtz.core.annotations.Execute;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;

@RequiredArgsConstructor
public class QuartzBeanPostProcessor implements BeanPostProcessor {
    private final CztQuartzJobRegistrar registrar;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopUtils.getTargetClass(bean);

        CztCronJob cztQuartzJob = AnnotationUtils.findAnnotation(
                bean.getClass(),
                CztCronJob.class);

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
            registrar.register(bean, cztQuartzJob, methodHandle);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return bean;
    }
}