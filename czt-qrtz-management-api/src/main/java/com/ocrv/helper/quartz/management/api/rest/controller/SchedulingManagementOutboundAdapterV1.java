package com.ocrv.helper.quartz.management.api.rest.controller;

import com.ocrv.helper.quartz.management.api.dto.JobDefDto;
import com.ocrv.helper.quartz.management.api.dto.JobDefInfoDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefCreateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefUpdateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerModifiedResult;
import com.ocrv.helper.quartz.management.api.rest.ex.CztRemoteSchedulerOperationException;
import com.ocrv.helper.quartz.management.api.model.RemoteAdapterInfo;
import com.ocrv.helper.quartz.management.api.model.RemoteSchedulerProps;
import com.ocrv.helper.quartz.management.api.model.TriggerDef;

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
