package com.ocrv.helper.quartz.management.core.adapter;

import com.ocrv.helper.quartz.management.api.SchedulerManager;
import com.ocrv.helper.quartz.management.api.rest.ex.SchedulerOperationException;
import com.ocrv.helper.quartz.management.api.rest.ex.TriggerNotFound;
import com.ocrv.helper.quartz.management.api.rest.ex.JobNotFound;
import com.ocrv.helper.quartz.management.api.model.JobDef;
import com.ocrv.helper.quartz.management.api.model.Result;
import com.ocrv.helper.quartz.management.api.model.TriggerDef;
import com.ocrv.helper.quartz.management.api.model.TriggerFilter;
import com.ocrv.helper.quartz.management.core.mapping.QuartzMapperUtils;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class QuartzSchedulerManagerAdapter implements SchedulerManager {
    protected final Scheduler scheduler;

    @Override
    public Result<JobDef<Map<String, Object>>> getJob(String id) {
        return defTry(() -> {
                    var jobDetail = scheduler.getJobDetail(QuartzMapperUtils.stringToJobKey(id));
                    if (jobDetail == null)
                        throw new JobNotFound("Job not found: " + id);
                    return jobDetail;
                })
                .map(this::mapJobDetail)
                .map(Result::success)
                .getOrElseGet(Result::failure);
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

    @Override
    public Set<String> listTriggerGroups() {
        return new HashSet<>(defTry(scheduler::getTriggerGroupNames).get());
    }

    @Override
    public List<TriggerDef> listTriggers(TriggerFilter filter) {
        return defTry(() -> {
            var groupMatcher = GroupMatcher.anyTriggerGroup();
            if (filter.hasGroup()) {
                var groupName = filter.group();
                groupMatcher = createGroupMatcher(groupName);
            }

            return scheduler.getTriggerKeys(groupMatcher);
        })
                .map(triggerKeys -> io.vavr.collection.Stream.ofAll(triggerKeys)
                        .map(key -> defTry(() -> getTriggerThrowing(key))
                                .flatMap(trigger -> defTry(() -> QuartzMapperUtils.fromQuartzTrigger(trigger,
                                        getTriggerStateSafe(trigger.getKey()).get())))
                        )
                        .filter(Try::isSuccess) // Фильтруем только успешные результаты
                        .map(Try::get)
                        // Применяем дополнительные фильтры
                        .filter(triggerDef -> matchesFilter(triggerDef, filter))
                        .toJavaList())
                .getOrElse(List::of);
    }

    @Override
    public List<TriggerDef> listTriggers() {
        return listTriggers(TriggerFilter.empty());
    }

    @Override
    public Result<TriggerDef> getTrigger(String id) {
        var key = QuartzMapperUtils.stringToTriggerKey(id);
        return defTry(() -> getTriggerThrowing(key))
                .flatMapTry(trigger -> defTry(() -> QuartzMapperUtils.fromQuartzTrigger(trigger,
                        getTriggerStateSafe(trigger.getKey()).get())))
                .map(Result::success)
                .getOrElseGet(Result::failure);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<Boolean> createTrigger(TriggerDef def,
                                         @Nullable LocalDateTime start,
                                         @Nullable LocalDateTime end) {
        var fn = defTry(() -> QuartzMapperUtils.toQuartzTrigger(def, start, end),
                e -> log.warn("Error creating trigger: {}, {}", def.getId(), e.getMessage()))
                .flatMapTry(it -> {
                    var qrtzJob = org.quartz.Job.class;
                    var jobData = new JobDataMap(def.getJobData());

                    return Optional.of(getJobDefinition(def.getJobId())).map(job -> {
                        if (Arrays.stream(job.getJobClass().getInterfaces())
                                .anyMatch(declaredInterface -> declaredInterface.isAssignableFrom(qrtzJob))) {
                            var targetJob = (Class<org.quartz.Job>) job.getJobClass();
                            var jobDetail = JobBuilder.newJob(targetJob)
                                    .ofType(targetJob)
                                    .withIdentity(new JobKey(job.getId(), def.getId()))
                                    .setJobData(jobData)
                                    .requestRecovery()
                                    .build();
                            return defTry(() -> scheduler.scheduleJob(jobDetail, it));
                        } else {
                            return Try.failure(new IllegalStateException(String.format("Provided job does not implement %s interface", qrtzJob)));
                        }
                    }).orElse(Try.failure(
                            new JobNotFound("Job not found or not registered, make sure you've registered job: " + def.getJobId())));
                });

        return mapBoolean(fn);
    }

    @Override
    public Result<Boolean> updateTrigger(
            TriggerDef def,
            @Nullable LocalDateTime start,
            @Nullable LocalDateTime end
    ) {
        var res = defTry(() -> {
            var newTrigger = QuartzMapperUtils.toQuartzTrigger(def, start, end);
            var key = newTrigger.getKey();
            return scheduler.rescheduleJob(key, newTrigger);
        }, e -> log.warn("Error updating trigger: {}", def.getId()));
        return mapBoolean(res);
    }

    @Override
    public Result<Boolean> stopTrigger(String id) {
        var key = QuartzMapperUtils.stringToTriggerKey(id);
        var res = defTry(() -> {
            var trigger = getTriggerThrowing(key);
            var existingKey = trigger.getKey();
            scheduler.pauseTrigger(existingKey);
            return null;
        }, e -> log.warn("Error stopping trigger: {}", id));
        return mapBoolean(res);
    }

    @Override
    public Result<Boolean> startTrigger(String id) {
        var key = QuartzMapperUtils.stringToTriggerKey(id);
        var res = defTry(() -> {
            var trigger = getTriggerThrowing(key);
            var existingKey = trigger.getKey();
            scheduler.resumeTrigger(existingKey);
            return null;
        }, e -> log.warn("Error starting trigger: {}", id));
        return mapBoolean(res);
    }

    private Result<Boolean> mapBoolean(Try<?> res) {
        return res.map(it -> Result.success(true))
                .onFailure(it -> log.debug("Failed trying invoke function", it))
                .recoverWith(it -> Try.success(Result.failure(it)))
                .get();
    }

    private <T> Try<T> defTry(CheckedFunction0<T> fn, Consumer<Throwable> onErrorLog) {
        return Try.of(fn).onFailure(onErrorLog);
    }

    private <T> Try<T> defTry(CheckedFunction0<T> fn) {
        return Try.of(fn).onFailure(ex ->
                log.warn("Exception while trying to execute quartz function: {}", ex.getMessage()));
    }

    private Try<TriggerState> getTriggerStateSafe(TriggerKey key) {
        return defTry(() -> scheduler.getTriggerState(key));
    }

    private boolean matchesFilter(TriggerDef triggerDef, TriggerFilter filter) {
        if (filter.hasId() && !matchesIdPattern(triggerDef.getId(), filter.id()))
            return false;

        if (filter.hasDescription() && !matchesDescriptionPattern(triggerDef.getDescription(), filter.description()))
            return false;

        if (filter.hasName()) {
            var name = filter.name();
            var nameMatcher = createNameMatcher(name);

            var triggerKey = QuartzMapperUtils.stringToTriggerKey(triggerDef.getId());
            if (!nameMatcher.isMatch(triggerKey)) {
                return false;
            }
        }

        if (filter.hasCronExpression()) {
            var cronExp = filter.cronExpression();
            if (triggerDef.getCronExpression() == null ||
                    (cronExp != null &&
                            !cronExp.equals(triggerDef.getCronExpression()))) {
                return false;
            }
        }

        if (filter.hasNextFireTimeFrom()) {
            var from = filter.nextFireTimeFrom();
            if (triggerDef.getNextFireTime() == null ||
                    triggerDef.getNextFireTime().isBefore(from)) {
                return false;
            }
        }

        if (filter.hasNextFireTimeTo()) {
            var to = filter.nextFireTimeTo();
            if (triggerDef.getNextFireTime() == null ||
                    triggerDef.getNextFireTime().isAfter(to)) {
                return false;
            }
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
        if (text == null) return false;

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

    private <T> T createPatternMatcher(@NonNull String pattern,
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

    private @NonNull Trigger getTriggerThrowing(@NonNull TriggerKey key) {
        Trigger trigger;
        try {
            trigger = scheduler.getTrigger(key);
        } catch (SchedulerException e) {
            throw new SchedulerOperationException(e);
        }

        if (trigger == null)
            throw new TriggerNotFound("Trigger not found: " + key);

        return trigger;
    }
}