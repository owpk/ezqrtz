package io.owpk.ezqrtz.management.api.rest.controller;

import io.owpk.ezqrtz.management.api.dto.JobDefDto;
import io.owpk.ezqrtz.management.api.dto.JobDefInfoDto;
import io.owpk.ezqrtz.management.api.dto.TriggerDefCreateDto;
import io.owpk.ezqrtz.management.api.dto.TriggerDefUpdateDto;
import io.owpk.ezqrtz.management.api.dto.TriggerModifiedResult;
import io.owpk.ezqrtz.management.api.rest.ex.EzRemoteSchedulerOperationException;
import io.owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import io.owpk.ezqrtz.management.api.model.TriggerDef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SchedulingManagementAdapterV1 {

    JobDefDto getJob(String id) throws EzRemoteSchedulerOperationException;

    JobDefInfoDto getJobDefinition(String id);

    List<JobDefInfoDto> listJobs();

    TriggerDef getTrigger(String id) throws EzRemoteSchedulerOperationException;

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
