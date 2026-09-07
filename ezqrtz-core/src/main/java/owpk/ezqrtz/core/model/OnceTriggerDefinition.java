package owpk.ezqrtz.core.model;

import java.time.Instant;

public record OnceTriggerDefinition(
        Instant instant
) implements TriggerDefinition {
}