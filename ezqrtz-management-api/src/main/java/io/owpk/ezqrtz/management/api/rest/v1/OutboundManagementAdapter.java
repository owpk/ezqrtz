package io.owpk.ezqrtz.management.api.rest.v1;

import io.owpk.ezqrtz.management.api.ex.RemoteSchedulerOperation;
import io.owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import io.owpk.ezqrtz.management.api.model.RemoteSchedulerProps;
import io.owpk.ezqrtz.management.api.model.TriggerDef;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefInfoDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefCreateDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefUpdateDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerModifiedResult;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@NullMarked
public interface OutboundManagementAdapter {

    List<RemoteSchedulerProps> listAdapters();

    JobDefDto getJob(
            String id,
            String adapterId) throws RemoteSchedulerOperation;

    JobDefInfoDto getJobDefinition(
            String id,
            String adapterId) throws RemoteSchedulerOperation;

    List<JobDefInfoDto> listJobs(
            String adapterId) throws RemoteSchedulerOperation;

    TriggerDef getTrigger(
            String id,
            String adapterId) throws RemoteSchedulerOperation;

    Set<String> listTriggerGroups(
            String adapterId) throws RemoteSchedulerOperation;

    List<TriggerDef> listTriggers(
            String adapterId,
            String id,
            String description,
            String group,
            String name,
            String cronExpression,
            LocalDateTime nextFireTimeFrom,
            LocalDateTime nextFireTimeTo) throws RemoteSchedulerOperation;

    TriggerModifiedResult createTrigger(
            TriggerDefCreateDto def,
            String adapterId,
            LocalDateTime startAt,
            LocalDateTime endAt) throws RemoteSchedulerOperation;

    TriggerModifiedResult updateTrigger(
            TriggerDefUpdateDto def,
            String adapterId,
            LocalDateTime startAt,
            LocalDateTime endAt) throws RemoteSchedulerOperation;

    TriggerModifiedResult startTrigger(
            String id,
            String adapterId) throws RemoteSchedulerOperation;

    TriggerModifiedResult stopTrigger(
            String id,
            String adapterId) throws RemoteSchedulerOperation;

    RemoteAdapterInfo getInfo(
            String adapterId) throws RemoteSchedulerOperation;

}
