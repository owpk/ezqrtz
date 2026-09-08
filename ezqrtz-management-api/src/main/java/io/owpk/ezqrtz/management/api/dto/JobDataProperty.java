package io.owpk.ezqrtz.management.api.dto;

import lombok.Builder;

@Builder
public record JobDataProperty(
        KnownTypes type,
        String value) {
}
