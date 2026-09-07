package com.ocrv.helper.quartz.management.api.rest.client;

import com.ocrv.helper.quartz.management.api.dto.JobDefDto;
import com.ocrv.helper.quartz.management.api.dto.JobDefInfoDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefCreateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerDefUpdateDto;
import com.ocrv.helper.quartz.management.api.dto.TriggerModifiedResult;
import com.ocrv.helper.quartz.management.api.rest.ex.CztRemoteSchedulerOperationException;
import com.ocrv.helper.quartz.management.api.rest.ex.CztSchedulerManagementException;
import com.ocrv.helper.quartz.management.api.InboundSchedulerManager;
import com.ocrv.helper.quartz.management.api.model.RemoteAdapterInfo;
import com.ocrv.helper.quartz.management.api.model.Result;
import com.ocrv.helper.quartz.management.api.model.TriggerDef;
import com.ocrv.helper.quartz.management.api.model.TriggerFilter;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class SchedulingManagementRestAdapterV1Impl implements SchedulingManagementRestAdapterV1 {

    //@formatter:off
    // URL mapping constants
    public static final String BASE_PATH =                  "/v1/scheduling/management";
    public static final String GET_JOB_PATH =               "/job";
    public static final String GET_JOB_DEF_PATH =           "/job/definition";
    public static final String LIST_JOBS_PATH =             "/job/definitions";
    public static final String GET_TRIGGER_PATH =           "/trigger";
    public static final String LIST_TRIGGER_GROUPS_PATH =   "/triggers/groups";
    public static final String LIST_TRIGGERS_PATH =         "/triggers";
    public static final String CREATE_TRIGGER_PATH =        "/trigger";
    public static final String START_TRIGGER_PATH =         "/trigger/start";
    public static final String STOP_TRIGGER_PATH =          "/trigger/stop";
    public static final String UPDATE_TRIGGER_PATH =        "/trigger";
    public static final String GET_INFO_PATH =              "/info";
    //@formatter:on

    private final InboundSchedulerManager inboundSchedulerManager;

    public SchedulingManagementRestAdapterV1Impl(InboundSchedulerManager inboundSchedulerManager) {
        this.inboundSchedulerManager = inboundSchedulerManager;
    }

    @Override
    public JobDefInfoDto getJobDefinition(String id) {
        return mapJobDef(inboundSchedulerManager.getJobDefinition(id));
    }

    @Override
    public List<JobDefInfoDto> listRegisteredJobs() {
        return inboundSchedulerManager.listJobs().stream()
                .map(this::mapJobDef)
                .toList();
    }

    @Override
    public JobDefDto getJob(String id) throws CztRemoteSchedulerOperationException {
        return inboundSchedulerManager.getJob(id)
                .map(this::mapJob)
                .getOrElseThrow(this::unwrapFailure);
    }

    @Override
    public TriggerDef getTrigger(String id) throws CztRemoteSchedulerOperationException {
        return inboundSchedulerManager.getTrigger(id)
                .getOrElseThrow(this::unwrapFailure);
    }

    /**
     * Контролируемые ошибки (not-found и т.п.) пробрасываются как есть,
     * чтобы Advice смаппил их в корректный HTTP-статус (404);
     * остальное оборачивается в CztRemoteSchedulerOperationException (500).
     */
    private RuntimeException unwrapFailure(Throwable err) {
        if (err instanceof CztSchedulerManagementException czt)
            return czt;
        return new CztRemoteSchedulerOperationException(err);
    }

    @Override
    public Set<String> listTriggerGroups() {
        return inboundSchedulerManager.listTriggerGroups();
    }

    @Override
    public List<TriggerDef> listTriggers(TriggerFilter filter) {
        return inboundSchedulerManager.listTriggers(filter);
    }

    @Override
    public TriggerModifiedResult createTrigger(TriggerDefCreateDto def,
                                               @Nullable LocalDateTime startAt,
                                               @Nullable LocalDateTime endAt) {
        var triggerDef = mapToTriggerDef(def);
        var result = inboundSchedulerManager.createTrigger(triggerDef, startAt, endAt);
        return buildTriggerResult(def.getId(), result, "created");
    }

    @Override
    public TriggerModifiedResult updateTrigger(TriggerDefUpdateDto def,
                                               @Nullable LocalDateTime startAt,
                                               @Nullable LocalDateTime endAt) {
        var triggerDef = mapToTriggerDef(def);
        var result = inboundSchedulerManager.updateTrigger(triggerDef, startAt, endAt);
        return buildTriggerResult(triggerDef.getId(), result, "updated");
    }

    @Override
    public TriggerModifiedResult startTrigger(String id) {
        var result = inboundSchedulerManager.startTrigger(id);
        return buildTriggerResult(id, result, "started");
    }

    @Override
    public TriggerModifiedResult stopTrigger(String id) {
        var result = inboundSchedulerManager.stopTrigger(id);
        return buildTriggerResult(id, result, "stopped");
    }

    private TriggerModifiedResult buildTriggerResult(String id, Result<Boolean> result, String action) {
        if (result.isFailure() && result.err() instanceof CztSchedulerManagementException czt)
            throw czt;

        boolean success = result.isSuccess() && result.getOrElse(false);
        String message = result.isSuccess() ?
                (result.getOrElse(false) ?
                        "Trigger successfully " + action :
                        "Failed to " + action + " trigger") :
                "Error: " + result.getMessageOr("Unknown error");

        return TriggerModifiedResult.builder()
                .id(id)
                .success(success)
                .message(message)
                .build();
    }

    @Override
    public RemoteAdapterInfo getInfo() {
        return inboundSchedulerManager.getInfo();
    }
}
