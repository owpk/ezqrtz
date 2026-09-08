package io.owpk.ezqrtz.management.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDefDto {
    private String id;
    private String description;
    private String jobClass;
    private Map<String, JobDataProperty> jobDataMap;
}
