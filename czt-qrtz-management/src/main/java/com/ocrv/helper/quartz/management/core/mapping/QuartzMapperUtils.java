package com.ocrv.helper.quartz.management.core.mapping;

import com.ocrv.helper.quartz.management.api.model.TriggerDef;
import com.ocrv.helper.quartz.management.api.model.TriggerState;
import org.jspecify.annotations.Nullable;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

/**
 * Facade for mapping Quartz objects to internal model and vice versa.
 * 
 * @author Vyacheslav Vorobyev
 */
public final class QuartzMapperUtils {

    private QuartzMapperUtils() {
    }

    public static String triggerKeyToString(TriggerKey key) {
        return key.getName() + (key.getGroup() != null ? ":" + key.getGroup() : "");
    }

    public static TriggerKey stringToTriggerKey(String key) {
        String[] parts = splitByLastColon(key);
        if (parts.length > 1)
            return new TriggerKey(parts[0], parts[1]);
        return new TriggerKey(parts[0]);
    }

    public static String jobKeyToString(JobKey key) {
        return key.getName() + (key.getGroup() != null ? ":" + key.getGroup() : "");
    }

    public static JobKey stringToJobKey(String key) {

        String[] parts = splitByLastColon(key);
        if (parts.length > 1)
            return new JobKey(parts[0], parts[1]);
        return new JobKey(parts[0]);
    }

    public static String[] splitByLastColon(String str) {
        int lastColonIndex = str.lastIndexOf(':');

        if (lastColonIndex == -1) {
            return new String[]{str};
        } else {
            // Разделяем по последнему двоеточию
            String firstPart = str.substring(0, lastColonIndex);
            String secondPart = str.substring(lastColonIndex + 1);
            return new String[]{firstPart, secondPart};
        }
    }


    public static Trigger toQuartzTrigger(TriggerDef def, @Nullable LocalDateTime startAt,
                                          @Nullable LocalDateTime endAt) {
        return TriggerMapper.toQuartzTrigger(def, toDate(startAt), toDate(endAt));
    }

    public static TriggerDef fromQuartzTrigger(Trigger trigger, Trigger.TriggerState state) {
        return TriggerMapper.fromQuartzTrigger(trigger, state);
    }

    private static @Nullable Date toDate(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

final class JobMapper {

    private JobMapper() {
    }

    @SuppressWarnings("unchecked") // NOSO
    private static Class<? extends org.quartz.Job> loadJobClass(Class<?> cl) {
        var jobClz = org.quartz.Job.class;
        if (Arrays.stream(cl.getInterfaces()).anyMatch(declaredI -> declaredI.isAssignableFrom(jobClz)))
            return (Class<? extends org.quartz.Job>) cl;
        throw new IllegalStateException("Provided job class does not implement org.quartz.Job interface: " + cl.toString());
    }
}

final class TriggerMapper {

    private TriggerMapper() {
    }

    public static Trigger toQuartzTrigger(TriggerDef def,
                                          @Nullable Date startAt,
                                          @Nullable Date endAt) {
        TriggerKey key;

        if (def.getId().contains(":")) {
            key = QuartzMapperUtils.stringToTriggerKey(def.getId());
        } else {
            key = new TriggerKey(def.getId());
        }

        var trigger = TriggerBuilder.newTrigger()
                .withIdentity(key)
                .withDescription(def.getDescription())
                .withSchedule(CronScheduleBuilder.cronSchedule(def.getCronExpression()));

        if (startAt != null)
            trigger.startAt(startAt);
        if (endAt != null)
            trigger.endAt(endAt);

        return trigger.build();
    }

    public static TriggerDef fromQuartzTrigger(Trigger trigger, Trigger.TriggerState state) {
        if (!(trigger instanceof CronTrigger cron)) {
            throw new IllegalArgumentException("Only CronTrigger is supported");
        }

        return TriggerDef.builder()
                .id(QuartzMapperUtils.triggerKeyToString(trigger.getKey()))
                .jobId(QuartzMapperUtils.jobKeyToString(trigger.getJobKey()))
                .name(trigger.getKey().getName())
                .jobData(trigger.getJobDataMap())
                .description(trigger.getDescription())
                .cronExpression(cron.getCronExpression())
                .state(convertState(state))
                .nextFireTime(toLocalDateTime(trigger.getNextFireTime()))
                .previousFireTime(toLocalDateTime(trigger.getPreviousFireTime()))
                .build();
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        return date != null ? LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : null;
    }

    private static TriggerState convertState(Trigger.TriggerState state) {
        return switch (state) {
            case PAUSED -> TriggerState.PAUSED;
            case COMPLETE -> TriggerState.COMPLETE;
            case ERROR -> TriggerState.ERROR;
            case BLOCKED -> TriggerState.BLOCKED;
            case NORMAL -> TriggerState.WORKING;
            default -> TriggerState.NONE;
        };
    }
}