package owpk.ezqrtz.management.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record JobDataCreateProperty(KnownTypes type, String value) {

    @JsonCreator
    public static JobDataCreateProperty create(
            @JsonProperty("type") Object typeValue,
            @JsonProperty("value") String value) {

        var type = KnownTypes.fromJson(typeValue);
        return new JobDataCreateProperty(type, value);
    }
}
