package owpk.ezqrtz.internal.model;

public sealed interface TriggerDefinition
        permits
        CronTriggerDefinition,
        OnceTriggerDefinition,
        RepeatTriggerDefinition {
}