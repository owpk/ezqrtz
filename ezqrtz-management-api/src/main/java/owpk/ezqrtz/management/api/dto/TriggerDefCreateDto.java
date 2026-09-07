package owpk.ezqrtz.management.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDefCreateDto {

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String jobId;

    @JsonProperty
    private Map<String, JobDataCreateProperty> jobData;

    /**
     * <a href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html">cron tutorial</a>
     */
    @JsonProperty(required = true)
    private String cronExpression;

    @JsonProperty
    private String description;
}
