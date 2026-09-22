package io.owpk.ezqrtz.management.api.rest.v1.dto;

import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Builder
@NullMarked
public record JobDefInfoDto(
        String id,
        @Nullable String description,
        String jobClass,
        @Nullable Map<String, KnownTypes> jobDataDefinition) {

    public JobDefInfoDto {
        if (description == null)
            description = "";
        if (jobDataDefinition == null)
            jobDataDefinition = Map.of();
    }
}
