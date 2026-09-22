package io.owpk.ezqrtz.management.adapter;

import io.owpk.ezqrtz.management.api.DescriableSchedulerManager;
import io.owpk.ezqrtz.management.api.model.Result;
import io.owpk.ezqrtz.management.api.rest.v1.MappingUtils;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefInfoDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefCreateDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefUpdateDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerModifiedResult;
import io.owpk.ezqrtz.management.api.rest.v1.RestConstants;
import io.owpk.ezqrtz.management.api.rest.v1.InboundManagementAdapter;
import io.owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import io.owpk.ezqrtz.management.api.model.TriggerDef;
import io.owpk.ezqrtz.management.api.model.TriggerFilter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(RestConstants.BASE_PATH)
@RequiredArgsConstructor
@NullMarked
public class InboundManagementController implements InboundManagementAdapter {

    private final DescriableSchedulerManager schedulingManager;

    @Override
    @GetMapping(RestConstants.GET_JOB_PATH)
    public JobDefDto getJob(@RequestParam("id") String id) {
        var decoded = decode(id);
        var result = schedulingManager.getJob(decoded);
        return MappingUtils.mapJob(result.getOrElseThrow());
    }

    @Override
    @GetMapping(RestConstants.GET_JOB_DEF_PATH)
    public JobDefInfoDto getJobDefinition(@RequestParam("id") String id) {
        var decoded = decode(id);
        var result = schedulingManager.getJobDefinition(decoded);
        return MappingUtils.mapJobDef(result);
    }

    @Override
    @GetMapping(RestConstants.LIST_JOBS_PATH)
    public List<JobDefInfoDto> listJobs() {
        var listJobs = schedulingManager.listJobs();
        return listJobs.stream().map(MappingUtils::mapJobDef).toList();
    }

    @Override
    @GetMapping(RestConstants.GET_TRIGGER_PATH)
    public TriggerDef getTrigger(@RequestParam("id") String id) {
        var decoded = decode(id);
        var triggerDef = schedulingManager.getTrigger(decoded);
        return triggerDef.getOrElseThrow();
    }

    @Override
    @GetMapping(RestConstants.LIST_TRIGGER_GROUPS_PATH)
    public Set<String> listTriggerGroups() {
        return schedulingManager.listTriggerGroups();
    }

    @Override
    @GetMapping(RestConstants.LIST_TRIGGERS_PATH)
    public List<TriggerDef> listTriggers(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "group", required = false) String group,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "cronExpression", required = false) String cronExpression,
            @RequestParam(value = "nextFireTimeFrom", required = false) LocalDateTime nextFireTimeFrom,
            @RequestParam(value = "nextFireTimeTo", required = false) LocalDateTime nextFireTimeTo) {

        var filter = TriggerFilter.builder()
                .id(decode(id))
                .description(description)
                .group(decode(group))
                .name(name)
                .cronExpression(decode(cronExpression))
                .nextFireTimeFrom(nextFireTimeFrom)
                .nextFireTimeTo(nextFireTimeTo)
                .build();
        return schedulingManager.listTriggers(filter);
    }

    @Override
    @PostMapping(RestConstants.CREATE_TRIGGER_PATH)
    public TriggerModifiedResult createTrigger(
            @RequestBody TriggerDefCreateDto def,
            @RequestParam(required = false) LocalDateTime startAt,
            @RequestParam(required = false) LocalDateTime endAt) {
        var trigger = MappingUtils.mapToTriggerDef(def);
        var result = schedulingManager.createTrigger(trigger, startAt, endAt);
        return toResult(def.id(), result, "create");
    }

    @Override
    @PutMapping(RestConstants.UPDATE_TRIGGER_PATH)
    public TriggerModifiedResult updateTrigger(
            @RequestBody TriggerDefUpdateDto def,
            @RequestParam(required = false) LocalDateTime startAt,
            @RequestParam(required = false) LocalDateTime endAt) {
        var trigger = MappingUtils.mapToTriggerDef(def);
        var result  =schedulingManager.updateTrigger(trigger, startAt, endAt);
        return toResult(def.id(), result, "update");
    }


    @Override
    @PutMapping(RestConstants.START_TRIGGER_PATH)
    public TriggerModifiedResult startTrigger(@RequestParam("id") String id) {
        var decoded = decode(id);
        var result = schedulingManager.startTrigger(decoded);
        return toResult(decoded, result, "start");
    }

    @Override
    @PutMapping(RestConstants.STOP_TRIGGER_PATH)
    public TriggerModifiedResult stopTrigger(@RequestParam("id") String id) {
        var decoded = decode(id);
        var result = schedulingManager.stopTrigger(decoded);
        return toResult(decoded, result, "stop");
    }

    @Override
    @GetMapping(RestConstants.GET_INFO_PATH)
    public RemoteAdapterInfo getInfo() {
        return schedulingManager.getInfo();
    }

    private TriggerModifiedResult toResult(String id, Result<?> r, String action) {
        return r.map(_ -> TriggerModifiedResult.builder()
                        .success(true)
                        .id(id)
                        .message(action)
                        .build())
                .getOrElseThrow();
    }

    private @Nullable String decode(@Nullable String id) {
        return Optional.ofNullable(id).map(it -> URLDecoder.decode(it, StandardCharsets.UTF_8)).orElse(null);
    }
}