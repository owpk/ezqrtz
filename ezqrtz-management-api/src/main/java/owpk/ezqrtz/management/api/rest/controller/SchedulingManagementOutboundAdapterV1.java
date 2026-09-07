package owpk.ezqrtz.management.api.rest.controller;

import owpk.ezqrtz.management.api.dto.JobDefDto;
import owpk.ezqrtz.management.api.dto.JobDefInfoDto;
import owpk.ezqrtz.management.api.dto.TriggerDefCreateDto;
import owpk.ezqrtz.management.api.dto.TriggerDefUpdateDto;
import owpk.ezqrtz.management.api.dto.TriggerModifiedResult;
import owpk.ezqrtz.management.api.rest.ex.CztRemoteSchedulerOperationException;
import owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import owpk.ezqrtz.management.api.model.RemoteSchedulerProps;
import owpk.ezqrtz.management.api.model.TriggerDef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SchedulingManagementOutboundAdapterV1 {

    List<RemoteSchedulerProps> listAdapters();

    JobDefDto getJob(
            String id,
            String adapterId) throws CztRemoteSchedulerOperationException;

    JobDefInfoDto getJobDefinition(
            String id,
            String adapterId);

    List<JobDefInfoDto> listJobs(
            String adapterId);

    TriggerDef getTrigger(
            String id,
            String adapterId) throws CztRemoteSchedulerOperationException;

    Set<String> listTriggerGroups(
            String adapterId);

    List<TriggerDef> listTriggers(
            String adapterId,
            String id,
            String description,
            String group,
            String name,
            String cronExpression,
            LocalDateTime nextFireTimeFrom,
            LocalDateTime nextFireTimeTo);

    TriggerModifiedResult createTrigger(
            TriggerDefCreateDto def,
            String adapterId,
            LocalDateTime startAt,
            LocalDateTime endAt);

    TriggerModifiedResult updateTrigger(
            TriggerDefUpdateDto def,
            String adapterId,
            LocalDateTime startAt,
            LocalDateTime endAt);

    TriggerModifiedResult startTrigger(
            String id,
            String adapterId);

    TriggerModifiedResult stopTrigger(
            String id,
            String adapterId);

    RemoteAdapterInfo getInfo(
            String adapterId);

}
