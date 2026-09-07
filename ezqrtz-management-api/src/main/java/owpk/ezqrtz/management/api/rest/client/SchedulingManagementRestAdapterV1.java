package owpk.ezqrtz.management.api.rest.client;

import owpk.ezqrtz.management.api.dto.JobDefDto;
import owpk.ezqrtz.management.api.dto.JobDefInfoDto;
import owpk.ezqrtz.management.api.dto.KnownTypes;
import owpk.ezqrtz.management.api.dto.TriggerDefCreateDto;
import owpk.ezqrtz.management.api.dto.TriggerDefUpdateDto;
import owpk.ezqrtz.management.api.dto.TriggerModifiedResult;
import owpk.ezqrtz.management.api.rest.JobDataMapper;
import owpk.ezqrtz.management.api.rest.ex.EzRemoteSchedulerOperationException;
import owpk.ezqrtz.management.api.model.JobDef;
import owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import owpk.ezqrtz.management.api.model.TriggerDef;
import owpk.ezqrtz.management.api.model.TriggerFilter;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface SchedulingManagementRestAdapterV1 {

    JobDefInfoDto getJobDefinition(String id);

    List<JobDefInfoDto> listRegisteredJobs();

    JobDefDto getJob(String id) throws EzRemoteSchedulerOperationException;

    TriggerDef getTrigger(String id) throws EzRemoteSchedulerOperationException;

    Set<String> listTriggerGroups();

    List<TriggerDef> listTriggers(TriggerFilter filter);

    TriggerModifiedResult createTrigger(TriggerDefCreateDto def,
                                        @Nullable LocalDateTime startAt,
                                        @Nullable LocalDateTime endAt);

    TriggerModifiedResult updateTrigger(TriggerDefUpdateDto def,
                                        @Nullable LocalDateTime startAt,
                                        @Nullable LocalDateTime endAt);

    TriggerModifiedResult startTrigger(String id);

    TriggerModifiedResult stopTrigger(String id);

    RemoteAdapterInfo getInfo();

    default TriggerDef mapToTriggerDef(TriggerDefCreateDto def) {
        var trigger = TriggerDef.builder()
                .cronExpression(def.getCronExpression())
                .description(def.getDescription())
                .id(def.getId())
                .jobId(def.getJobId());

        if (def.getJobData() != null)
            trigger.jobData(def.getJobData().entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            it -> JobDataMapper.mapToObj(it.getValue())
                                    .orElseThrow(() ->
                                            new IllegalArgumentException("Cannot deserialize property: " + it.getValue())))));

        return trigger.build();
    }

    default TriggerDef mapToTriggerDef(TriggerDefUpdateDto def) {
        var trigger = TriggerDef.builder()
                .cronExpression(def.getCronExpression())
                .description(def.getDescription())
                .id(def.getId());

        return trigger.build();
    }

    default JobDefDto mapJob(JobDef<Map<String, Object>> job) {
        var jobData = job.getData().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        it -> JobDataMapper.mapToJobDataProperty(it.getValue())));

        return JobDefDto.builder()
                .jobClass(job.getJobClass().getName())
                .jobDataMap(jobData)
                .description(job.getDescription())
                .id(job.getId())
                .build();
    }

    default JobDefInfoDto mapJobDef(JobDef<Map<String, Class<?>>> job) {
        return JobDefInfoDto.builder()
                .jobClass(job.getJobClass().getName())
                .jobDataDefinition(job.getData().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                it -> KnownTypes.of(it.getValue().getName())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Unsupported job data type: " + it.getValue().getName())))))
                .description(job.getDescription())
                .id(job.getId())
                .build();
    }

}
