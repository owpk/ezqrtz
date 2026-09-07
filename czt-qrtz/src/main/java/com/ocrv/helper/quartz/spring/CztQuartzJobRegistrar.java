package com.ocrv.helper.quartz.spring;

import com.ocrv.helper.quartz.core.DefaultCztQuartzScheduleExecutor;
import com.ocrv.helper.quartz.core.annotations.CztCronJob;
import com.ocrv.helper.quartz.core.api.SchedulerInterceptor;
import com.ocrv.helper.quartz.core.model.CronTriggerDefinition;
import com.ocrv.helper.quartz.core.model.ScheduleRequest;
import com.ocrv.helper.quartz.core.model.SchedulerNamespace;
import com.ocrv.helper.quartz.core.utils.QrtzUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public record CztQuartzJobRegistrar(
        Scheduler scheduler,
        List<SchedulerInterceptor> interceptors,
        Map<UUID, CztQuartzJobRegistration> registrations,
        DefaultCztQuartzScheduleExecutor executor
) {

    @Autowired
    public CztQuartzJobRegistrar(Scheduler scheduler, List<SchedulerInterceptor> interceptors) {
        this(scheduler, interceptors,
                new ConcurrentHashMap<>(),
                new DefaultCztQuartzScheduleExecutor(scheduler, interceptors));
    }

    public void register(UUID id, CztQuartzJobRegistration registration) {
        registrations.put(id, registration);
    }

    public void register(Object bean, CztCronJob cztQuartzJob, MethodHandle methodHandle) {
        register(UUID.randomUUID(), new CztQuartzJobRegistration(bean, cztQuartzJob, methodHandle));
    }

    public void initAll() {
        registrations.forEach((id, it) -> {
            var anno = it.annotation();
            var namespace = SchedulerNamespace.builder()
                    .triggerGroup(anno.group())
                    .jobGroup(anno.group())
                    .build();

            var request = ScheduleRequest.builder()
                    .triggerIdentity(anno.name())
                    .jobIdentity(anno.name())
                    .jobClass(SpringJobBridge.class)
                    .durable(true)
                    .description(anno.description())
                    .jobDataCustomizer(jobDataMap -> jobDataMap.put("__handler", id.toString()))
                    .collisionStrategy(anno.collisionStrategy().getStrategy())
                    .trigger(CronTriggerDefinition.of(anno.cron(), anno.zoneId()))
                    .build();

            var result = executor.schedule(request, namespace);
            QrtzUtils.logScheduleResult(result, executor);
        });
    }

    public Optional<CztQuartzJobRegistration> get(String beanId) {
        return Optional.ofNullable(registrations.get(UUID.fromString(beanId)));
    }
}