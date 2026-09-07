package owpk.ezqrtz.management.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerDef {
    private String id;
    private String jobId;

    @Singular("jobParam")
    private Map<String, Object> jobData;

    private String name;
    private String description;
    private String cronExpression;

    @Builder.Default
    private TriggerState state = TriggerState.NONE;

    private LocalDateTime nextFireTime;
    private LocalDateTime previousFireTime;
}
