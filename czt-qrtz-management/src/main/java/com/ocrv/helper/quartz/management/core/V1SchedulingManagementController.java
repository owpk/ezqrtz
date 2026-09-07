package com.ocrv.helper.quartz.management.core;

import com.ocrv.helper.quartz.management.api.dto.JobDefDto;
import com.ocrv.helper.quartz.management.api.dto.JobDefInfoDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefCreateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefUpdateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerModifiedResult;
import com.ocrv.helper.quartz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl;
import com.ocrv.helper.quartz.management.api.rest.controller.SchedulingManagementAdapterV1;
import com.ocrv.helper.quartz.management.api.model.RemoteAdapterInfo;
import com.ocrv.helper.quartz.management.api.model.TriggerDef;
import com.ocrv.helper.quartz.management.api.model.TriggerFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(SchedulingManagementRestAdapterV1Impl.BASE_PATH)
@RequiredArgsConstructor
public class V1SchedulingManagementController implements SchedulingManagementAdapterV1 {

    private final SchedulingManagementRestAdapterV1Impl schedulingManager;

    @Override
    @GetMapping(SchedulingManagementRestAdapterV1Impl.GET_JOB_PATH)
    public JobDefDto getJob(@RequestParam("id") String id) {
        return schedulingManager.getJob(id);
    }

    @Override
    @GetMapping(SchedulingManagementRestAdapterV1Impl.GET_JOB_DEF_PATH)
    public JobDefInfoDto getJobDefinition(@RequestParam("id") String id) {
        return schedulingManager.getJobDefinition(id);
    }

    @Override
    @GetMapping(SchedulingManagementRestAdapterV1Impl.LIST_JOBS_PATH)
    public List<JobDefInfoDto> listJobs() {
        return schedulingManager.listRegisteredJobs();
    }

    @Override
    @GetMapping(SchedulingManagementRestAdapterV1Impl.GET_TRIGGER_PATH)
    public TriggerDef getTrigger(@RequestParam("id") String id) {
        return schedulingManager.getTrigger(id);
    }

    @Override
    @GetMapping(SchedulingManagementRestAdapterV1Impl.LIST_TRIGGER_GROUPS_PATH)
    public Set<String> listTriggerGroups() {
        return schedulingManager.listTriggerGroups();
    }

    @Override
    @GetMapping(SchedulingManagementRestAdapterV1Impl.LIST_TRIGGERS_PATH)
    public List<TriggerDef> listTriggers(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "group", required = false) String group,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "cronExpression", required = false) String cronExpression,
            @RequestParam(value = "nextFireTimeFrom", required = false) LocalDateTime nextFireTimeFrom,
            @RequestParam(value = "nextFireTimeTo", required = false) LocalDateTime nextFireTimeTo) {

        var filter = TriggerFilter.builder()
                .id(id)
                .description(description)
                .group(group)
                .name(name)
                .cronExpression(cronExpression)
                .nextFireTimeFrom(nextFireTimeFrom)
                .nextFireTimeTo(nextFireTimeTo)
                .build();
        return schedulingManager.listTriggers(filter);
    }

    @Override
    @PostMapping(SchedulingManagementRestAdapterV1Impl.CREATE_TRIGGER_PATH)
    public TriggerModifiedResult createTrigger(
            @RequestBody TriggerDefCreateDto def,
            @RequestParam(required = false) LocalDateTime startAt,
            @RequestParam(required = false) LocalDateTime endAt) {
        return schedulingManager.createTrigger(def, startAt, endAt);
    }

    @Override
    @PutMapping(SchedulingManagementRestAdapterV1Impl.UPDATE_TRIGGER_PATH)
    public TriggerModifiedResult updateTrigger(
            @RequestBody TriggerDefUpdateDto def,
            @RequestParam(required = false) LocalDateTime startAt,
            @RequestParam(required = false) LocalDateTime endAt) {
        return schedulingManager.updateTrigger(def, startAt, endAt);
    }


    @Override
    @PutMapping(SchedulingManagementRestAdapterV1Impl.START_TRIGGER_PATH)
    public TriggerModifiedResult startTrigger(@RequestParam("id") String id) {
        return schedulingManager.startTrigger(id);
    }

    @Override
    @PutMapping(SchedulingManagementRestAdapterV1Impl.STOP_TRIGGER_PATH)
    public TriggerModifiedResult stopTrigger(@RequestParam("id") String id) {
        return schedulingManager.stopTrigger(id);
    }

    @Override
    @GetMapping(SchedulingManagementRestAdapterV1Impl.GET_INFO_PATH)
    public RemoteAdapterInfo getInfo() {
        return schedulingManager.getInfo();
    }
}
