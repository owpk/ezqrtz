package owpk.ezqrtz.management.api.rest.client;

import owpk.ezqrtz.management.api.dto.JobDefDto;
import owpk.ezqrtz.management.api.dto.JobDefInfoDto;
import owpk.ezqrtz.management.api.dto.TriggerModifiedResult;
import owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import owpk.ezqrtz.management.api.model.TriggerDef;
import owpk.ezqrtz.management.api.model.TriggerFilter;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.GET_INFO_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.BASE_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.CREATE_TRIGGER_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.GET_JOB_DEF_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.GET_JOB_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.GET_TRIGGER_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.LIST_JOBS_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.LIST_TRIGGERS_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.LIST_TRIGGER_GROUPS_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.START_TRIGGER_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.STOP_TRIGGER_PATH;
import static owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl.UPDATE_TRIGGER_PATH;

public record SchedulerManagementV1RestClientHelper(String baseUrl) {

    public JobDefInfoDto getJobDefinition(String id, Function<String, JobDefInfoDto> getProvider) {
        var url = buildUrl(map -> map.put("id", id), GET_JOB_DEF_PATH);
        return getProvider.apply(url);
    }

    public JobDefDto getJob(String id, Function<String, JobDefDto> getProvider) {
        var url = buildUrl(map -> map.put("id", id), GET_JOB_PATH);
        return getProvider.apply(url);
    }

    public List<JobDefInfoDto> listJobs(Function<String, List<JobDefInfoDto>> getProvider) {
        var url = buildUrl(LIST_JOBS_PATH);
        return getProvider.apply(url);
    }

    public TriggerDef getTrigger(String id, Function<String, TriggerDef> getProvider) {
        var url = buildUrl(query -> query.put("id", id), GET_TRIGGER_PATH);
        return getProvider.apply(url);
    }

    public Set<String> listTriggerGroups(Function<String, Set<String>> getProvider) {
        var url = buildUrl(LIST_TRIGGER_GROUPS_PATH);
        return getProvider.apply(url);
    }

    public List<TriggerDef> listTriggers(TriggerFilter filter,
                                         Function<String, List<TriggerDef>> getProvider) {
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
        return getProvider.apply(url);
    }

    public TriggerModifiedResult createTrigger(@Nullable LocalDateTime startAt,
                                               @Nullable LocalDateTime endAt,
                                               Function<String, TriggerModifiedResult> postProvider) {
        var url = buildUrl(map -> {
            if (startAt != null)
                map.put("startAt", startAt.toString());

            if (endAt != null)
                map.put("endAt", endAt.toString());
        }, CREATE_TRIGGER_PATH);
        return postProvider.apply(url);
    }

    public TriggerModifiedResult updateTrigger(@Nullable LocalDateTime startAt,
                                               @Nullable LocalDateTime endAt,
                                               Function<String, TriggerModifiedResult> putProvider) {
        var url = buildUrl(map -> {
            if (startAt != null)
                map.put("startAt", startAt.toString());

            if (endAt != null)
                map.put("endAt", endAt.toString());
        }, UPDATE_TRIGGER_PATH);
        return putProvider.apply(url);
    }

    public TriggerModifiedResult startTrigger(String id,
                                              Function<String, TriggerModifiedResult> putProvider) {
        var url = buildUrl(query -> query.put("id", id), START_TRIGGER_PATH);
        return putProvider.apply(url);
    }

    public TriggerModifiedResult stopTrigger(String id,
                                             Function<String, TriggerModifiedResult> putProvider) {
        var url = buildUrl(query -> query.put("id", id), STOP_TRIGGER_PATH);
        return putProvider.apply(url);
    }

    public RemoteAdapterInfo getInfo(Function<String, RemoteAdapterInfo> getProvider) {
        var url = buildUrl(GET_INFO_PATH);
        return getProvider.apply(url);
    }

    private String buildUrl(String... more) {
        var joined = String.join("/", more);
        return baseUrl + BASE_PATH + joined;
    }

    private String buildUrl(Consumer<Map<String, String>> queryBuilder, String... path) {
        var params = new HashMap<String, String>();
        queryBuilder.accept(params);

        var query = params.entrySet().stream()
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .reduce((v1, v2) -> v1 + "&" + v2)
                .map(result -> "?" + result)
                .orElse("");

        var joined = String.join("", path);
        return baseUrl + BASE_PATH + joined + query;
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}