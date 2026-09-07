package com.ocrv.helper.quartz.core.model;

public sealed interface TriggerDefinition
        permits
        CronTriggerDefinition,
        OnceTriggerDefinition,
        RepeatTriggerDefinition {
}