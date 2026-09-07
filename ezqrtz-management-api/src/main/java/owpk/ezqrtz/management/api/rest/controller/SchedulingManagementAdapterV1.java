package owpk.ezqrtz.management.api.rest.controller;

import owpk.ezqrtz.management.api.dto.JobDefDto;
import owpk.ezqrtz.management.api.dto.JobDefInfoDto;
import owpk.ezqrtz.management.api.dto.TriggerDefCreateDto;
import owpk.ezqrtz.management.api.dto.TriggerDefUpdateDto;
import owpk.ezqrtz.management.api.dto.TriggerModifiedResult;
import owpk.ezqrtz.management.api.rest.ex.CztRemoteSchedulerOperationException;
import owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import owpk.ezqrtz.management.api.model.TriggerDef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SchedulingManagementAdapterV1 {

    JobDefDto getJob(String id) throws CztRemoteSchedulerOperationException;

    JobDefInfoDto getJobDefinition(String id);

    List<JobDefInfoDto> listJobs();

    TriggerDef getTrigger(String id) throws CztRemoteSchedulerOperationException;

    Set<String> listTriggerGroups();

    List<TriggerDef> listTriggers(
            String id,
            String description,
            String group,
            String name,
            String cronExpression,
            LocalDateTime nextFireTimeFrom,
            LocalDateTime nextFireTimeTo);

    TriggerModifiedResult createTrigger(
            TriggerDefCreateDto def,
            LocalDateTime startAt,
            LocalDateTime endAt);

    TriggerModifiedResult updateTrigger(
            TriggerDefUpdateDto def,
            LocalDateTime startAt,
            LocalDateTime endAt);

    TriggerModifiedResult startTrigger(String id);

    TriggerModifiedResult stopTrigger(String id);

    RemoteAdapterInfo getInfo();

}
