package io.owpk.ezqrtz.core.model;

public sealed interface TriggerDefinition
        permits
        CronTriggerDefinition,
        OnceTriggerDefinition,
        RepeatTriggerDefinition {
}