package com.ocrv.helper.quartz.management.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDef<T> {
    private String id;
    private String description;
    private Class<?> jobClass;
    private T data;
}
