package io.owpk.ezqrtz.management.api.rest.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html">cron tutorial</a>
 */
@Builder
public record TriggerDefUpdateDto(

        @JsonProperty(required = true)
        String id,

        @JsonProperty(required = true)
        String cronExpression,

        @JsonProperty
        String description
) {}
