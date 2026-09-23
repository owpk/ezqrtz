package io.owpk.ezqrtz.management.api.rest.v1;

import io.owpk.ezqrtz.management.api.ex.EzSchedulerManagementException;
import io.owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
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

/**
 * Входной порт для управления via Rest
 */
@NullMarked
public interface InboundManagementAdapter {

    JobDefDto getJob(String id) throws EzSchedulerManagementException;

    JobDefInfoDto getJobDefinition(String id) throws EzSchedulerManagementException;

    List<JobDefInfoDto> listJobs() throws EzSchedulerManagementException;

    TriggerDef getTrigger(String id) throws EzSchedulerManagementException;

    Set<String> listTriggerGroups() throws EzSchedulerManagementException;

    List<TriggerDef> listTriggers(
            String id,
            String description,
            String group,
            String name,
            String cronExpression,
            LocalDateTime nextFireTimeFrom,
            LocalDateTime nextFireTimeTo) throws EzSchedulerManagementException;

    TriggerModifiedResult createTrigger(
            TriggerDefCreateDto def,
            LocalDateTime startAt,
            LocalDateTime endAt) throws EzSchedulerManagementException;

    TriggerModifiedResult updateTrigger(
            TriggerDefUpdateDto def,
            LocalDateTime startAt,
            LocalDateTime endAt) throws EzSchedulerManagementException;

    TriggerModifiedResult startTrigger(String id) throws EzSchedulerManagementException;

    TriggerModifiedResult stopTrigger(String id) throws EzSchedulerManagementException;

    RemoteAdapterInfo getInfo() throws EzSchedulerManagementException;

}