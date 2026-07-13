package owpk.ezqrtz.spring;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import owpk.ezqrtz.annotations.CronTriggerJob;
import owpk.ezqrtz.api.SchedulerInterceptor;
import owpk.ezqrtz.internal.DefaultEzQuartzScheduleExecutor;
import owpk.ezqrtz.internal.model.CronTriggerDefinition;
import owpk.ezqrtz.internal.model.ScheduleRequest;
import owpk.ezqrtz.internal.model.SchedulerNamespace;
import owpk.ezqrtz.utils.QrtzUtils;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public record EzQuartzJobRegistrar(
        Scheduler scheduler,
        List<SchedulerInterceptor> interceptors,
        Map<UUID, EzQuartzJobRegistration> registrations,
        DefaultEzQuartzScheduleExecutor executor
) {

    @Autowired
    public EzQuartzJobRegistrar(Scheduler scheduler, List<SchedulerInterceptor> interceptors) {
        this(scheduler, interceptors,
                new ConcurrentHashMap<>(),
                new DefaultEzQuartzScheduleExecutor(scheduler, interceptors));
    }

    public void register(UUID id, EzQuartzJobRegistration registration) {
        registrations.put(id, registration);
    }

    public void register(Object bean, CronTriggerJob ezQuartzJob, MethodHandle methodHandle) {
        register(UUID.randomUUID(), new EzQuartzJobRegistration(bean, ezQuartzJob, methodHandle));
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

    public Optional<EzQuartzJobRegistration> get(String beanId) {
        return Optional.ofNullable(registrations.get(UUID.fromString(beanId)));
    }
}