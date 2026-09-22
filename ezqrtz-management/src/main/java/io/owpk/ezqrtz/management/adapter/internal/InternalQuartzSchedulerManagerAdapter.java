package io.owpk.ezqrtz.management.adapter.internal;

import io.owpk.ezqrtz.management.api.SchedulerManager;
import io.owpk.ezqrtz.management.api.ex.CztSchedulerManagementException;
import io.owpk.ezqrtz.management.api.ex.JobNotFound;
import io.owpk.ezqrtz.management.api.ex.SchedulerOperation;
import io.owpk.ezqrtz.management.api.ex.TriggerNotFound;
import io.owpk.ezqrtz.management.api.model.JobDef;
import io.owpk.ezqrtz.management.api.model.Result;
import io.owpk.ezqrtz.management.api.model.TriggerDef;
import io.owpk.ezqrtz.management.api.model.TriggerFilter;
import io.owpk.ezqrtz.management.mapping.QuartzMapperUtils;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.NameMatcher;
import org.quartz.utils.DirtyFlagMap;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@NullMarked
public abstract class InternalQuartzSchedulerManagerAdapter implements SchedulerManager {
    protected final Scheduler scheduler;

    @Override
    public Result<JobDef<Map<String, Object>>> getJob(String id) {
        return def(() -> {
            var key = QuartzMapperUtils.stringToJobKey(id);
            var jobDetail = scheduler.getJobDetail(key);
            if (jobDetail == null)
                throw new JobNotFound(id);
            return mapJobDetail(jobDetail);
        });
    }

    @Override
    public Set<String> listTriggerGroups() {
        var result = def(scheduler::getTriggerGroupNames);
        return result.map(HashSet::new)
                .getOrElseThrow();
    }

    @Override
    public List<TriggerDef> listTriggers(TriggerFilter filter) {
        Supplier<Set<TriggerKey>> safeSupplier = () -> def(() -> {
            var groupMatcher = GroupMatcher.anyTriggerGroup();
            if (filter.hasGroup()) {
                var groupName = filter.group();
                if (Objects.nonNull(groupName))
                    groupMatcher = createGroupMatcher(groupName);
            }
            return scheduler.getTriggerKeys(groupMatcher);
        }).getOrElseThrow();

        return defTryS(safeSupplier)
                .map(triggerKeys -> io.vavr.collection.Stream.ofAll(triggerKeys)
                        .map(this::getTriggerThrowing)
                        .map(trigger -> {
                                    var triggerState = getTriggerStateSafe(trigger.getKey()).getOrElseThrow();
                                    return QuartzMapperUtils.fromQuartzTrigger(trigger, triggerState);
                                }
                        )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        // Применяем дополнительные фильтры
                        .filter(triggerDef -> matchesFilter(triggerDef, filter))
                        .toJavaList()).get();
    }

    @Override
    public List<TriggerDef> listTriggers() {
        return listTriggers(TriggerFilter.empty());
    }

    @Override
    public Result<TriggerDef> getTrigger(String id) {
        var key = QuartzMapperUtils.stringToTriggerKey(id);
        Supplier<Trigger> quartKey = () -> getTriggerThrowing(key);

        return defMap(defTryS(quartKey).map(trigger -> {
            var state = getTriggerStateSafe(trigger.getKey()).getOrElseThrow();
            return QuartzMapperUtils.fromQuartzTrigger(trigger, state)
                    .orElseThrow(() -> new IllegalStateException("No value"));
        }));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<Boolean> createTrigger(TriggerDef def,
                                         @Nullable LocalDateTime start,
                                         @Nullable LocalDateTime end) {
        var fn = defTryF(() -> QuartzMapperUtils.toQuartzTrigger(def, start, end))
                .flatMapTry(it -> {
                    var qrtzJob = org.quartz.Job.class;
                    var jobData = new JobDataMap(def.jobData());

                    return Optional.of(getJobDefinition(def.jobId())).map(job -> {
                        if (Arrays.stream(job.jobClass().getInterfaces())
                                .anyMatch(declaredInterface -> declaredInterface.isAssignableFrom(qrtzJob))) {
                            var targetJob = (Class<org.quartz.Job>) job.jobClass();
                            var jobDetail = JobBuilder.newJob(targetJob)
                                    .ofType(targetJob)
                                    .withIdentity(new JobKey(job.id(), def.id()))
                                    .setJobData(jobData)
                                    .requestRecovery()
                                    .build();
                            return defTryF(() -> scheduler.scheduleJob(jobDetail, it));
                        } else {
                            return Try.failure(new IllegalStateException(String.format("Provided job does not implement %s interface", qrtzJob)));
                        }
                    }).orElse(Try.failure(
                            new JobNotFound("Job not found or not registered, make sure you've registered job: " + def.jobId())));
                });

        return mapBoolean(fn);
    }

    @Override
    public Result<Boolean> updateTrigger(
            TriggerDef def,
            @Nullable LocalDateTime start,
            @Nullable LocalDateTime end
    ) {
        var res = defTryF(() -> {
            var newTrigger = QuartzMapperUtils.toQuartzTrigger(def, start, end);
            var key = newTrigger.getKey();
            return scheduler.rescheduleJob(key, newTrigger);
        });
        return mapBoolean(res);
    }

    @Override
    public Result<Boolean> stopTrigger(String id) {
        var res = defTryF(() -> {
            var key = QuartzMapperUtils.stringToTriggerKey(id);
            var trigger = getTriggerThrowing(key);
            var existingKey = trigger.getKey();
            scheduler.pauseTrigger(existingKey);
            return true;
        });
        return mapBoolean(res);
    }

    @Override
    public Result<Boolean> startTrigger(String id) {
        var res = defTryF(() -> {
            var key = QuartzMapperUtils.stringToTriggerKey(id);
            var trigger = getTriggerThrowing(key);
            var existingKey = trigger.getKey();
            scheduler.resumeTrigger(existingKey);
            return true;
        });
        return mapBoolean(res);
    }

    private Result<TriggerState> getTriggerStateSafe(TriggerKey key) {
        return def(() -> scheduler.getTriggerState(key));
    }

    private boolean matchesFilter(TriggerDef triggerDef, TriggerFilter filter) {
        if (filter.hasId() && !matchesIdPattern(triggerDef.id(), filter.id()))
            return false;

        if (filter.hasDescription() && !matchesDescriptionPattern(triggerDef.description(),
                filter.description()))
            return false;

        if (filter.hasName()) {
            var name = filter.name();
            var nameMatcher = createNameMatcher(name);

            var triggerKey = QuartzMapperUtils.stringToTriggerKey(triggerDef.id());
            if (!nameMatcher.isMatch(triggerKey)) {
                return false;
            }
        }

        if (filter.hasCronExpression()) {
            var cronExp = filter.cronExpression();
            if (triggerDef.cronExpression() == null ||
                    (cronExp != null &&
                            !cronExp.equals(triggerDef.cronExpression()))) {
                return false;
            }
        }

        if (filter.hasNextFireTimeFrom()) {
            var from = filter.nextFireTimeFrom();
            if (triggerDef.nextFireTime() == null ||
                    triggerDef.nextFireTime().isBefore(from)) {
                return false;
            }
        }

        if (filter.hasNextFireTimeTo()) {
            var to = filter.nextFireTimeTo();
            return triggerDef.nextFireTime() != null &&
                    !triggerDef.nextFireTime().isAfter(to);
        }

        return true;
    }

    private interface PatternMatcherFactory<T> {
        T createContainsMatcher(String pattern);

        T createEndsWithMatcher(String pattern);

        T createStartsWithMatcher(String pattern);

        T createEqualsMatcher(String pattern);
    }

    private boolean matchesIdPattern(String actualId, String pattern) {
        return matchesPattern(actualId, pattern, true);
    }

    private boolean matchesDescriptionPattern(String actualDescription, String pattern) {
        return matchesPattern(actualDescription, pattern, false);
    }

    private boolean matchesPattern(String text, String pattern, boolean caseSensitive) {
        var actualText = caseSensitive ? text : text.toLowerCase();

        return createPatternMatcher(pattern, new PatternMatcherFactory<Boolean>() {
            @Override
            public Boolean createContainsMatcher(String searchText) {
                return actualText.contains(caseSensitive ? searchText : searchText.toLowerCase());
            }

            @Override
            public Boolean createEndsWithMatcher(String searchText) {
                return actualText.endsWith(caseSensitive ? searchText : searchText.toLowerCase());
            }

            @Override
            public Boolean createStartsWithMatcher(String searchText) {
                return actualText.startsWith(caseSensitive ? searchText : searchText.toLowerCase());
            }

            @Override
            public Boolean createEqualsMatcher(String searchText) {
                return actualText.equals(caseSensitive ? searchText : searchText.toLowerCase());
            }
        }, caseSensitive);
    }

    private NameMatcher<TriggerKey> createNameMatcher(String name) {
        return createPatternMatcher(name,
                NameMatcher::triggerNameContains,
                NameMatcher::triggerNameEndsWith,
                NameMatcher::triggerNameStartsWith,
                NameMatcher::triggerNameEquals,
                true);
    }

    private GroupMatcher<TriggerKey> createGroupMatcher(String groupName) {
        return createPatternMatcher(groupName,
                GroupMatcher::groupContains,
                GroupMatcher::groupEndsWith,
                GroupMatcher::groupStartsWith,
                GroupMatcher::groupEquals,
                true);
    }

    private <T> T createPatternMatcher(String pattern,
                                       Function<String, T> containsFactory,
                                       Function<String, T> endsWithFactory,
                                       Function<String, T> startsWithFactory,
                                       Function<String, T> equalsFactory,
                                       boolean caseSensitive) {
        return createPatternMatcher(pattern, new PatternMatcherFactory<>() {
            @Override
            public T createContainsMatcher(String p) {
                return containsFactory.apply(p);
            }

            @Override
            public T createEndsWithMatcher(String p) {
                return endsWithFactory.apply(p);
            }

            @Override
            public T createStartsWithMatcher(String p) {
                return startsWithFactory.apply(p);
            }

            @Override
            public T createEqualsMatcher(String p) {
                return equalsFactory.apply(p);
            }
        }, caseSensitive);
    }

    private <T> T createPatternMatcher(String pattern,
                                       PatternMatcherFactory<T> factory,
                                       boolean caseSensitive) {
        String processedPattern = caseSensitive ? pattern : pattern.toLowerCase();

        boolean startsWithAsterisk = processedPattern.startsWith("*");
        boolean endsWithAsterisk = processedPattern.endsWith("*");

        if (startsWithAsterisk && endsWithAsterisk) {
            String searchText = processedPattern.substring(1, processedPattern.length() - 1);
            return factory.createContainsMatcher(searchText);
        } else if (startsWithAsterisk) {
            String searchText = processedPattern.substring(1);
            return factory.createEndsWithMatcher(searchText);
        } else if (endsWithAsterisk) {
            String searchText = processedPattern.substring(0, processedPattern.length() - 1);
            return factory.createStartsWithMatcher(searchText);
        } else {
            return factory.createEqualsMatcher(processedPattern);
        }
    }

    private Trigger getTriggerThrowing(TriggerKey key) {
        Trigger trigger;
        try {
            trigger = scheduler.getTrigger(key);
        } catch (SchedulerException e) {
            throw new SchedulerOperation(e);
        }

        if (trigger == null)
            throw new TriggerNotFound("Trigger not found: " + key);

        return trigger;
    }

    private JobDef<Map<String, Object>> mapJobDetail(JobDetail job) {
        var jobData = Optional.ofNullable(job.getJobDataMap())
                .map(DirtyFlagMap::getWrappedMap)
                .orElse(Collections.emptyMap());

        return JobDef.<Map<String, Object>>builder()
                .id(QuartzMapperUtils.jobKeyToString(job.getKey()))
                .description(job.getDescription())
                .jobClass(job.getJobClass())
                .data(jobData)
                .build();
    }

    // ------- utils --------
    private Result<Boolean> mapBoolean(Try<?> res) {
        return defMap(res).map(_ -> true);
    }

    private <T> Try<T> defTryS(Supplier<T> fn) {
        return wrap(Try.ofSupplier(fn));
    }

    private <T> Try<T> defTryF(CheckedFunction0<T> fn) {
        return wrap(Try.of(fn));
    }

    private <T> Try<T> wrap(Try<T> t) {
        return t.onFailure(ex -> log.warn("Exception while trying to execute quartz function: {}", ex.getMessage()));
    }

    private <T> Result<T> defMap(Try<T> t) {
        return def(t::get);
    }

    private <T> Result<T> def(CheckedFunction0<T> fn) {
        return defTryF(fn)
                .map(Result::success)
                .getOrElseGet(e -> {
                    if (e instanceof SchedulerException se)
                        return Result.failure(new CztSchedulerManagementException(se));
                    if (e instanceof CztSchedulerManagementException ce)
                        return Result.failure(ce);
                    return Result.failure(new CztSchedulerManagementException(e));
                });
    }
}