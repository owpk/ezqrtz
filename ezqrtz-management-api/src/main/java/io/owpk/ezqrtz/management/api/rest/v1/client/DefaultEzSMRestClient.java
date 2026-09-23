package io.owpk.ezqrtz.management.api.rest.v1.client;

import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefInfoDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefCreateDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefUpdateDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerModifiedResult;
import io.owpk.ezqrtz.management.api.ex.RemoteSchedulerOperation;
import io.owpk.ezqrtz.management.api.ex.EzSchedulerManagementException;
import io.owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import io.owpk.ezqrtz.management.api.model.TriggerDef;
import io.owpk.ezqrtz.management.api.model.TriggerFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.BASE_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.CREATE_TRIGGER_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.GET_INFO_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.GET_JOB_DEF_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.GET_JOB_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.GET_TRIGGER_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.LIST_JOBS_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.LIST_TRIGGERS_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.LIST_TRIGGER_GROUPS_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.START_TRIGGER_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.STOP_TRIGGER_PATH;
import static io.owpk.ezqrtz.management.api.rest.v1.RestConstants.UPDATE_TRIGGER_PATH;

@Slf4j
@NullMarked
@RequiredArgsConstructor
public abstract class DefaultEzSMRestClient implements EzSMRestClient {

    private final String baseUrl;
    private final boolean debug;

    // Underlying transport providers
    protected abstract Function<String, JobDefInfoDto> getJobDefInfoProvider();
    protected abstract Function<String, JobDefDto> getJobDefProvider();
    protected abstract Function<String, List<JobDefInfoDto>> getJobDefListInfoProvider();
    protected abstract Function<String, TriggerDef> getTriggerDefProvider();
    protected abstract Function<String, Set<String>> getTriggerGroupsProvider();
    protected abstract Function<String, List<TriggerDef>> getListTriggersProvider();
    protected abstract Function<String, RemoteAdapterInfo> getAdapterInfoProvider();

     protected abstract BiFunction<String, TriggerDefCreateDto, TriggerModifiedResult> postCreateTriggerProvider();
     protected abstract BiFunction<String, TriggerDefUpdateDto, TriggerModifiedResult> putUpdateTriggerProvider();
     protected abstract Function<String, TriggerModifiedResult> putStartTriggerProvider();
     protected abstract Function<String, TriggerModifiedResult> putStopTriggerProvider();

    @Override
    public JobDefInfoDto getJobDefinition(String id) {
        var url = buildUrl(map ->
                map.put("id", id), GET_JOB_DEF_PATH);
        return getJobDefInfoProvider().apply(url);
    }

    @Override
    public JobDefDto getJob(String id) throws RemoteSchedulerOperation {
        var url = buildUrl(map ->
                map.put("id", id), GET_JOB_PATH);
        return getJobDefProvider().apply(url);
    }

    @Override
    public TriggerDef getTrigger(String id) throws RemoteSchedulerOperation {
        var url = buildUrl(query ->
                query.put("id", id), GET_TRIGGER_PATH);
        return getTriggerDefProvider().apply(url);
    }

    @Override
    public Set<String> listTriggerGroups() {
        var url = buildUrl(LIST_TRIGGER_GROUPS_PATH);
        return getTriggerGroupsProvider().apply(url);
    }

    @Override
    public List<TriggerDef> listTriggers(String id, String description, String group, String name, String cronExpression, LocalDateTime nextFireTimeFrom, LocalDateTime nextFireTimeTo) throws EzSchedulerManagementException {
        var filter = TriggerFilter.builder()
                .id(id)
                .group(group)
                .name(name)
                .description(description)
                .cronExpression(cronExpression)
                .nextFireTimeFrom(nextFireTimeFrom)
                .nextFireTimeTo(nextFireTimeTo).build();
        return listTriggers(filter);
    }

    @Override
    public List<TriggerDef> listTriggers(TriggerFilter filter) {
        var url = buildUrl(map -> {
            if (filter.hasId())
                map.put("id", filter.id());

            if (filter.hasDescription())
                map.put("description", filter.description());

            if (filter.hasNextFireTimeFrom())
                map.put("nextFireTimeFrom", filter.nextFireTimeFrom().toString());

            if (filter.hasNextFireTimeTo())
                map.put("nextFireTimeTo", filter.nextFireTimeTo().toString());

            if (filter.hasGroup())
                map.put("group", filter.group());

            if (filter.hasName())
                map.put("name", filter.name());

            if (filter.hasCronExpression())
                map.put("cronExpression", filter.cronExpression());

        }, LIST_TRIGGERS_PATH);
        return getListTriggersProvider().apply(url);
    }

    @Override
    public TriggerModifiedResult createTrigger(TriggerDefCreateDto def,
                                               @Nullable LocalDateTime startAt,
                                               @Nullable LocalDateTime endAt) {
        var url = buildUrl(map -> {
            if (startAt != null)
                map.put("startAt", startAt.toString());

            if (endAt != null)
                map.put("endAt", endAt.toString());
        }, CREATE_TRIGGER_PATH);
        return postCreateTriggerProvider().apply(url, def);
    }

    @Override
    public TriggerModifiedResult updateTrigger(TriggerDefUpdateDto def,
                                               @Nullable LocalDateTime startAt,
                                               @Nullable LocalDateTime endAt) {
        var url = buildUrl(map -> {
            if (startAt != null)
                map.put("startAt", startAt.toString());

            if (endAt != null)
                map.put("endAt", endAt.toString());
        }, UPDATE_TRIGGER_PATH);
        return putUpdateTriggerProvider().apply(url, def);
    }

    @Override
    public TriggerModifiedResult startTrigger(String id) {
        var url = buildUrl(query -> query.put("id", id), START_TRIGGER_PATH);
        return putStartTriggerProvider().apply(url);
    }

    @Override
    public TriggerModifiedResult stopTrigger(String id) {
        var url = buildUrl(query -> query.put("id", id), STOP_TRIGGER_PATH);
        return putStopTriggerProvider().apply(url);
    }

    @Override
    public RemoteAdapterInfo getInfo() {
        var url = buildUrl(GET_INFO_PATH);
        return getAdapterInfoProvider().apply(url);
    }

    @Override
    public List<JobDefInfoDto> listJobs() throws EzSchedulerManagementException {
        var url = buildUrl(LIST_JOBS_PATH);
        return getJobDefListInfoProvider().apply(url);
    }

    private String buildUrl(String... more) {
        var joined = String.join("/", more);
        return baseUrl + BASE_PATH + joined;
    }

    private String buildUrl(Consumer<Map<String, String>> queryBuilder, String... path) {
        var params = new HashMap<String, String>();
        queryBuilder.accept(params);

        var query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .reduce((v1, v2) -> v1 + "&" + v2)
                .map(result -> "?" + result)
                .orElse("");

        var joined = String.join("", path);
        var result = baseUrl + BASE_PATH + joined + query;
        if(debug)
            log.info("Building scheduling management request url: {}", result);
        return result;
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
