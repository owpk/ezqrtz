package owpk.ezqrtz.api;

import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for key-value pairs of the Quartz job data map.
 *
 * @author Vyacheslav Vorobev
 */
@Builder(toBuilder = true)
public record JobDataBuilder(
        Map<String, Object> values
) {

    /**
     * Default constructor, creates an empty job data map builder.
     */
    public JobDataBuilder() {
        this(new HashMap<>());
    }

    /**
     * Adds a key-value pair to the job data map.
     *
     * @param key   the key
     * @param value the value
     * @return this builder for method chaining
     */
    public JobDataBuilder put(String key, Object value) {
        values.put(key, value);
        return this;
    }
}