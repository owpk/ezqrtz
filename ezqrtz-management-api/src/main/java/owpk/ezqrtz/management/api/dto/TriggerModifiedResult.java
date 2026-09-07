package owpk.ezqrtz.management.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerModifiedResult {
    private String id;
    private boolean success;
    private String message;
}
