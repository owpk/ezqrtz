package io.owpk.ezqrtz.management.api.model;

import lombok.Builder;

@Builder
public record RemoteAdapterInfo(
        String identity,
        String name,
        String description,
        SchedulerStatus status,
        SchedulerType type) {

    public RemoteAdapterInfo {
        if (description == null)
            description = "";
    }
}
