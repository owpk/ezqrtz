package com.ocrv.helper.quartz.core.model;

import java.time.Instant;

public record OnceTriggerDefinition(
        Instant instant
) implements TriggerDefinition {
}