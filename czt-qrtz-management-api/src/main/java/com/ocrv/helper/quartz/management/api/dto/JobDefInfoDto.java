package com.ocrv.helper.quartz.management.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDefInfoDto {
    private String id;
    private String description;
    private String jobClass;
    private Map<String, KnownTypes> jobDataDefinition;
}
