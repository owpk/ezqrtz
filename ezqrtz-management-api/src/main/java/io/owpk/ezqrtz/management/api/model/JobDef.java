package io.owpk.ezqrtz.management.api.model;

import lombok.Builder;

@Builder
public record JobDef<T>(
        String id,
        String description,
        Class<?> jobClass,
        T data
) {

    public JobDef {
        if (description == null)
            description = "";
    }
}
