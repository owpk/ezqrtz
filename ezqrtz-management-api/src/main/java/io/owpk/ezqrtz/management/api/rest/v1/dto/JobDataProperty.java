package io.owpk.ezqrtz.management.api.rest.v1.dto;

import lombok.Builder;

@Builder
public record JobDataProperty(
        KnownTypes type,
        String value) {
}
