package com.ocrv.helper.quartz.management.api.dto;

import lombok.Builder;

@Builder
public record JobDataProperty(
        KnownTypes type,
        String value) {
}
