package owpk.ezqrtz.core;

import owpk.ezqrtz.core.api.CollisionStrategy;
import owpk.ezqrtz.core.api.EzQuartzScheduleExecutor;
import owpk.ezqrtz.core.api.QuartzTriggerNamesapce;
import owpk.ezqrtz.core.api.SchedulerInterceptor;
import owpk.ezqrtz.core.exception.SchedulerOperationException;
import owpk.ezqrtz.core.model.CronTriggerDefinition;
import owpk.ezqrtz.core.model.OnceTriggerDefinition;
import owpk.ezqrtz.core.model.RepeatTriggerDefinition;
import owpk.ezqrtz.core.model.ScheduleRequest;
import owpk.ezqrtz.core.model.ScheduleResult;
import owpk.ezqrtz.core.model.SchedulerNamespace;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Реализация {@link EzQuartzScheduleExecutor} по умолчанию.
 * <p>
 * Инкапсулирует работу с Quartz {@link Scheduler}: создание, удаление,
 * приостановку и возобновление задач и триггеров. Все операции с ключами
 * выполняются через {@link SchedulerNamespace}, что обеспечивает изоляцию
 * групп jobs/triggers. Перед каждой операцией планирования вызываются
 * зарегистрированные {@link SchedulerInterceptor}.
 *
 * @author Vyacheslav Vorobev
 */
@Slf4j
public class DefaultEzQuartzScheduleExecutor implements EzQuartzScheduleExecutor {

    protected final List<SchedulerInterceptor> interceptors;
    protected final QuartzTriggerNamesapce namespace;
    @Getter
    private final Scheduler scheduler;

    public DefaultEzQuartzScheduleExecutor(Scheduler scheduler,
                                           List<SchedulerInterceptor> interceptors) {
        this.namespace = new SchedulerNamespace();
        this.scheduler = scheduler;
        this.interceptors = interceptors;
    }

    public DefaultEzQuartzScheduleExecutor(Scheduler scheduler,
                                           List<SchedulerInterceptor> interceptors,
                                           QuartzTriggerNamesapce namespace) {
        this.namespace = namespace;
        this.scheduler = scheduler;
        this.interceptors = interceptors;
    }

    public DefaultEzQuartzScheduleExecutor(Scheduler scheduler,
                                           QuartzTriggerNamesapce namespace) {
        this(scheduler, List.of(), namespace);
    }

    public DefaultEzQuartzScheduleExecutor(Scheduler scheduler,
                                           String triggerGroup,
                                           String jobGroup) {
        this(scheduler, List.of(), SchedulerNamespace.builder()
                .triggerGroup(triggerGroup)
                .jobGroup(jobGroup)
                .build());
    }

    /**
     * Планирует новую задачу согласно {@link ScheduleRequest}.
     * <p>
     * Сначала разрешается коллизия с уже существующей задачей через
     * {@link CollisionStrategy}. Если коллизия не разрешена — задача не
     * планируется. Перед и после планирования вызываются перехватчики
     * {@link SchedulerInterceptor#beforeSchedule} и
     * {@link SchedulerInterceptor#afterSchedule} соответственно.
     *
     * @param request         описание параметров планирования
     * @param quartzNamesapce - пространство имен групп Quartz
     * @return результат с флагом {@code scheduled} и временем следующего запуска
     * @throws SchedulerOperationException если Quartz выбросил {@link SchedulerException}
     */
    public ScheduleResult schedule(ScheduleRequest request, QuartzTriggerNamesapce quartzNamesapce) {
        try {
            if (!resolveCollision(quartzNamesapce, request.collisionStrategy(), request.jobIdentity(),
                    () -> createJobDetails(request, quartzNamesapce),
                    () -> createTrigger(request, quartzNamesapce))) {
                return ScheduleResult.builder().scheduled(false)
                        .appliedStrategy(request.collisionStrategy())
                        .build();
            }

            interceptors.forEach(it ->
                    it.beforeSchedule(request));

            var jobDetail = createJobDetails(request, quartzNamesapce);
            var trigger = createTrigger(request, quartzNamesapce);

            var nextFireTime = scheduler.scheduleJob(jobDetail, trigger);

            log.debug("Job '{}' scheduled. Next fire time: {}", jobDetail.getKey(), nextFireTime);

            interceptors.forEach(it ->
                    it.afterSchedule(request,
                            LocalDate.ofInstant(nextFireTime.toInstant(), ZoneId.systemDefault())));

            return ScheduleResult.builder()
                    .scheduled(true)
                    .nextFireTime(nextFireTime == null ? null : nextFireTime.toInstant())
                    .appliedStrategy(request.collisionStrategy())
                    .build();
        } catch (SchedulerException ex) {
            log.error("Failed to schedule job: {}", ex.getMessage());
            throw new SchedulerOperationException("Failed to schedule job: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ScheduleResult schedule(ScheduleRequest request) {
        return schedule(request, namespace);
    }

    /**
     * Перепланирует существующий триггер новыми параметрами из {@link ScheduleRequest}.
     * <p>
     * Если триггер не найден — возвращает {@code scheduled=false} без исключения.
     * Перед и после перепланирования вызываются перехватчики
     * {@link SchedulerInterceptor#beforeReschedule} и
     * {@link SchedulerInterceptor#afterReschedule}.
     *
     * @param request новые параметры планирования
     * @return результат с флагом {@code scheduled} и обновлённым временем следующего запуска
     * @throws SchedulerOperationException если Quartz выбросил {@link SchedulerException}
     */
    @Override
    public ScheduleResult reschedule(ScheduleRequest request) {
        try {
            var triggerKey = namespace.triggerKey(request.triggerIdentity());
            var oldTrigger = scheduler.getTrigger(triggerKey);
            if (oldTrigger == null) {
                log.warn("Trigger '{}' does not exist. Skipping reschedule.", triggerKey);
                return ScheduleResult.builder().scheduled(false)
                        .appliedStrategy(request.collisionStrategy()).build();
            }

            interceptors.forEach(it ->
                    it.beforeReschedule(request));

            var trigger = createTrigger(request, namespace);
            var nextFireTime = scheduler.rescheduleJob(triggerKey, trigger);

            interceptors.forEach(it ->
                    it.afterReschedule(request,
                            LocalDate.ofInstant(nextFireTime.toInstant(), ZoneId.systemDefault())));

            return ScheduleResult.builder()
                    .scheduled(true)
                    .nextFireTime(nextFireTime.toInstant())
                    .appliedStrategy(request.collisionStrategy())
                    .build();
        } catch (SchedulerException ex) {
            log.error("Failed to reschedule job: {}", ex.getMessage());
            throw new SchedulerOperationException("Failed to schedule job: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean pauseJob(String identity) {
        return trySafe(() -> {
            scheduler.pauseJob(namespace.jobKey(identity));
            return true;
        }, _ -> false);
    }

    @Override
    public boolean pauseTrigger(String identity) {
        return trySafe(() -> {
            scheduler.pauseTrigger(namespace.triggerKey(identity));
            return true;
        }, _ -> false);
    }

    @Override
    public boolean resumeJob(String identity) {
        return trySafe(() -> {
            scheduler.resumeJob(namespace.jobKey(identity));
            return true;
        }, _ -> false);
    }

    @Override
    public boolean resumeTrigger(String identity) {
        return trySafe(() -> {
            scheduler.resumeTrigger(namespace.triggerKey(identity));
            return true;
        }, _ -> false);
    }

    @Override
    public boolean deleteJob(String identity) {
        return trySafe(() -> {
            return scheduler.deleteJob(namespace.jobKey(identity));
        }, _ -> false);
    }

    @Override
    public boolean jobExists(String identity) {
        return trySafe(() -> {
            return scheduler.checkExists(namespace.jobKey(identity));
        }, _ -> false);
    }

    @Override
    public boolean triggerExists(String identity) {
        return trySafe(() -> {
            return scheduler.checkExists(namespace.triggerKey(identity));
        }, _ -> false);
    }

    @Override
    public void pauseJob(String identity, Consumer<Exception> onError) {
        tryOr(() -> scheduler.pauseJob(namespace.jobKey(identity)), onError);
    }

    @Override
    public void pauseTrigger(String identity, Consumer<Exception> onError) {
        tryOr(() -> scheduler.pauseTrigger(namespace.triggerKey(identity)), onError);
    }

    @Override
    public void resumeJob(String identity, Consumer<Exception> onError) {
        tryOr(() -> scheduler.resumeJob(namespace.jobKey(identity)), onError);
    }

    @Override
    public void resumeTrigger(String identity, Consumer<Exception> onError) {
        tryOr(() -> scheduler.resumeTrigger(namespace.triggerKey(identity)), onError);
    }

    @Override
    public void deleteJob(String identity, Consumer<Exception> onError) {
        tryOr(() -> scheduler.deleteJob(namespace.jobKey(identity)), onError);
    }

    @Override
    public Optional<Trigger> getTrigger(String identity) {
        return trySafe(() -> {
            var trigger = scheduler.getTrigger(namespace.triggerKey(identity));
            return Optional.ofNullable(trigger);
        }, _ -> Optional.empty());
    }

    @Override
    public TriggerKey getTriggerKey(String identity) {
        return namespace.triggerKey(identity);
    }

    @Override
    public Optional<JobDetail> getJob(String identity) {
        return trySafe(() -> {
            var jobDetail = scheduler.getJobDetail(namespace.jobKey(identity));
            return Optional.ofNullable(jobDetail);
        }, _ -> Optional.empty());
    }

    @Override
    public JobKey getJobKey(String identity) {
        return namespace.jobKey(identity);
    }

    /**
     * Проверяет, существует ли уже задача с указанным идентификатором,
     * и если да — делегирует разрешение коллизии стратегии {@link CollisionStrategy}.
     *
     * @param collisionStrategy стратегия обработки коллизии
     * @param jobIdentity       идентификатор задачи
     * @return {@code true}, если можно создавать новую задачу; {@code false}, если стратегия запретила
     * @throws SchedulerException при ошибке обращения к планировщику
     */
    public boolean resolveCollision(
            QuartzTriggerNamesapce quartzNamesapce,
            CollisionStrategy collisionStrategy,
            String jobIdentity,
            Supplier<JobDetail> jobDetailFactory,
            Supplier<Trigger> triggerFactory
    ) throws SchedulerException {
        var jobKey = quartzNamesapce.jobKey(jobIdentity);

        if (!scheduler.checkExists(jobKey))
            return true;

        return collisionStrategy.handle(
                scheduler,
                jobKey,
                jobDetailFactory,
                triggerFactory);
    }

    /**
     * Создаёт {@link JobDetail} на основе параметров из {@link ScheduleRequest}:
     * класс задачи, идентификатор, описание, JobDataMap, флаги durability и recovery.
     *
     * @param request параметры планирования
     * @return готовый {@link JobDetail}
     */
    public JobDetail createJobDetails(ScheduleRequest request, QuartzTriggerNamesapce quartzNamesapce) {
        var jobDataMap = new JobDataMap();
        request.jobDataCustomizer().accept(jobDataMap);

        var builder = newJob(request.jobClass())
                .withIdentity(quartzNamesapce.jobKey(request.jobIdentity()))
                .usingJobData(jobDataMap);

        if (request.description() != null)
            builder.withDescription(request.description());

        if (Boolean.TRUE.equals(request.durable()))
            builder.storeDurably();

        if (request.requestRecovery())
            builder.requestRecovery();

        return builder.build();
    }

    /**
     * Создаёт {@link Trigger} на основе типа расписания из {@link ScheduleRequest}.
     * <p>
     * Поддерживаются три типа:
     * <ul>
     *   <li>{@link CronTriggerDefinition} — cron-выражение</li>
     *   <li>{@link OnceTriggerDefinition} — одиночный запуск в заданный момент</li>
     *   <li>{@link RepeatTriggerDefinition} — повторяющийся запуск с интервалом</li>
     * </ul>
     * Если задан {@link ScheduleRequest#triggerCustomizer()}, он применяется к builder'у.
     *
     * @param request параметры планирования
     * @return готовый {@link Trigger}
     */
    public Trigger createTrigger(ScheduleRequest request, QuartzTriggerNamesapce quartzNamesapce) {
        var builder = newTrigger()
                .withIdentity(quartzNamesapce.triggerKey(request.triggerIdentity()));

        if (Objects.nonNull(request.description()))
            builder.withDescription(request.description());

        if (Objects.nonNull(request.triggerCustomizer()))
            request.triggerCustomizer().accept(builder);

        if (Objects.nonNull(request.trigger()))
            switch (request.trigger()) {
                case CronTriggerDefinition cron -> {
                    var csb = cronSchedule(cron.cronString());
                    if (Objects.nonNull(cron.timeZone()))
                        csb.inTimeZone(cron.timeZone());
                    builder.withSchedule(csb);
                }
                case OnceTriggerDefinition once -> builder.startAt(Date.from(once.instant()));
                case RepeatTriggerDefinition repeat -> builder.startNow()
                        .withSchedule(simpleSchedule()
                                .withIntervalInMilliseconds(repeat.interval().toMillis())
                                .withRepeatCount(repeat.repeatCount())
                        );
            }

        return builder.build();
    }

    private <T> T trySafe(CheckedCallable<T> callable, Function<Exception, T> onError) {
        try {
            return callable.call();
        } catch (Exception ex) {
            log.warn("Failed to execute callable: {}", ex.getMessage());
            return onError.apply(ex);
        }
    }

    private void tryOr(CheckedRunnable runnable, Consumer<Exception> onError) {
        try {
            runnable.run();
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    interface CheckedCallable<T> {
        T call() throws Exception;
    }

    interface CheckedRunnable {
        void run() throws Exception;
    }

}