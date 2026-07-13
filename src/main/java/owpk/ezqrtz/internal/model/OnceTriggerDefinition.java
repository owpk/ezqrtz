package owpk.ezqrtz.internal.model;

import java.time.Instant;

public record OnceTriggerDefinition(
        Instant instant
) implements TriggerDefinition {
}