package owpk.ezqrtz.internal;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import owpk.ezqrtz.api.CollisionStrategy;
import owpk.ezqrtz.api.EzQuartzScheduleExecutor;
import owpk.ezqrtz.api.QuartzTriggerNamesapce;
import owpk.ezqrtz.api.SchedulerInterceptor;
import owpk.ezqrtz.exception.SchedulerOperationException;
import owpk.ezqrtz.internal.model.CronTriggerDefinition;
import owpk.ezqrtz.internal.model.OnceTriggerDefinition;
import owpk.ezqrtz.internal.model.RepeatTriggerDefinition;
import owpk.ezqrtz.internal.model.ScheduleRequest;
import owpk.ezqrtz.internal.model.ScheduleResult;
import owpk.ezqrtz.internal.model.SchedulerNamespace;

/**
 * Default implementation of {@link EzQuartzScheduleExecutor}.
 * <p>
 * Encapsulates work with the Quartz {@link Scheduler}: creation, deletion,
 * suspension and resumption of jobs and triggers. All operations with keys
 * are performed through {@link SchedulerNamespace}, ensuring isolation of
 * job/trigger groups. Before each scheduling operation, registered
 * {@link SchedulerInterceptor} instances are invoked.
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
     * Schedules a new job according to {@link ScheduleRequest}.
     * <p>
     * First, a collision with an existing job is resolved through
     * {@link CollisionStrategy}. If the collision is not resolved — the job is not
     * scheduled. Before and after scheduling, interceptors
     * {@link SchedulerInterceptor#beforeSchedule} and
     * {@link SchedulerInterceptor#afterSchedule} are called, respectively.
     *
     * @param request         the scheduling parameters description
     * @param quartzNamesapce - Quartz group namespace
     * @return result with {@code scheduled} flag and next fire time
     * @throws SchedulerOperationException if Quartz throws a {@link SchedulerException}
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
     * Reschedules an existing trigger with new parameters from {@link ScheduleRequest}.
     * <p>
     * If the trigger is not found — returns {@code scheduled=false} without exception.
     * Before and after rescheduling, interceptors
     * {@link SchedulerInterceptor#beforeReschedule} and
     * {@link SchedulerInterceptor#afterReschedule} are called.
     *
     * @param request new scheduling parameters
     * @return result with {@code scheduled} flag and updated next fire time
     * @throws SchedulerOperationException if Quartz throws a {@link SchedulerException}
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
     * Checks whether a job with the specified identity already exists,
     * and if it does, delegates collision resolution to the {@link CollisionStrategy}.
     *
     * @param collisionStrategy collision handling strategy
     * @param jobIdentity       job identity
     * @return {@code true} if a new job can be created; {@code false} if the strategy prohibited it
     * @throws SchedulerException if an error occurs while accessing the scheduler
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
     * Creates a {@link JobDetail} based on the parameters from {@link ScheduleRequest}:
     * the job class, identity, description, JobDataMap, and the durability/recovery flags.
     *
     * @param request scheduling parameters
     * @return a ready {@link JobDetail}
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
     * Creates a {@link Trigger} based on the schedule type from {@link ScheduleRequest}.
     * <p>
     * Three types are supported:
     * <ul>
     *   <li>{@link CronTriggerDefinition} — a cron expression</li>
     *   <li>{@link OnceTriggerDefinition} — a single execution at a specified moment</li>
     *   <li>{@link RepeatTriggerDefinition} — a repeated execution at a given interval</li>
     * </ul>
     * If {@link ScheduleRequest#triggerCustomizer()} is provided, it is applied to the builder.
     *
     * @param request scheduling parameters
     * @return a ready {@link Trigger}
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