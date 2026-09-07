package com.ocrv.helper.quartz.management.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoteAdapterInfo {
    private String identity;
    private String name;
    private String description;
    private SchedulerStatus status;
    private SchedulerType type;
}
