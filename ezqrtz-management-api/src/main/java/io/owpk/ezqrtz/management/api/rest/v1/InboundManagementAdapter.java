package io.owpk.ezqrtz.management.api.rest.v1;

import io.owpk.ezqrtz.management.api.ex.CztSchedulerManagementException;
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

    JobDefDto getJob(String id) throws CztSchedulerManagementException;

    JobDefInfoDto getJobDefinition(String id) throws CztSchedulerManagementException;

    List<JobDefInfoDto> listJobs() throws CztSchedulerManagementException;

    TriggerDef getTrigger(String id) throws CztSchedulerManagementException;

    Set<String> listTriggerGroups() throws CztSchedulerManagementException;

    List<TriggerDef> listTriggers(
            String id,
            String description,
            String group,
            String name,
            String cronExpression,
            LocalDateTime nextFireTimeFrom,
            LocalDateTime nextFireTimeTo) throws CztSchedulerManagementException;

    TriggerModifiedResult createTrigger(
            TriggerDefCreateDto def,
            LocalDateTime startAt,
            LocalDateTime endAt) throws CztSchedulerManagementException;

    TriggerModifiedResult updateTrigger(
            TriggerDefUpdateDto def,
            LocalDateTime startAt,
            LocalDateTime endAt) throws CztSchedulerManagementException;

    TriggerModifiedResult startTrigger(String id) throws CztSchedulerManagementException;

    TriggerModifiedResult stopTrigger(String id) throws CztSchedulerManagementException;

    RemoteAdapterInfo getInfo() throws CztSchedulerManagementException;

}