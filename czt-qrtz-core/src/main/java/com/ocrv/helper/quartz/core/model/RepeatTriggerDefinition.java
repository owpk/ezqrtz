package com.ocrv.helper.quartz.core.model;

import java.time.Duration;

public record RepeatTriggerDefinition(
        Duration interval,
        int repeatCount
) implements TriggerDefinition {

    public static final int FOREVER = -1;

}