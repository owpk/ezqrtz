package com.ocrv.helper.quartz.management.api.rest.controller;

import com.ocrv.helper.quartz.management.api.dto.JobDefDto;
import com.ocrv.helper.quartz.management.api.dto.JobDefInfoDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefCreateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefUpdateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerModifiedResult;
import com.ocrv.helper.quartz.management.api.rest.ex.CztRemoteSchedulerOperationException;
import com.ocrv.helper.quartz.management.api.model.RemoteAdapterInfo;
import com.ocrv.helper.quartz.management.api.model.TriggerDef;

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
