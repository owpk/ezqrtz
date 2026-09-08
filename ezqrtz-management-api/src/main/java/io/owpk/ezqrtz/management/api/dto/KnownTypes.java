package io.owpk.ezqrtz.management.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum KnownTypes {
    STRING("java.lang.String"),
    INTEGER("java.lang.Integer"),
    LONG("java.lang.Long"),
    DOUBLE("java.lang.Double"),
    FLOAT("java.lang.Float"),
    BOOLEAN("java.lang.Boolean"),
    DATE("java.util.Date"),
    LOCAL_DATE("java.time.LocalDate"),
    LOCAL_DATE_TIME("java.time.LocalDateTime"),
    LOCAL_TIME("java.time.LocalTime"),
    OFFSET_DATE_TIME("java.time.OffsetDateTime"),
    OFFSET_TIME("java.time.OffsetTime"),
    ZONED_DATE_TIME("java.time.ZonedDateTime"),
    INSTANT("java.time.Instant"),
    UUID("java.util.UUID"),
    BIG_DECIMAL("java.math.BigDecimal"),
    BIG_INTEGER("java.math.BigInteger"),
    BYTE_ARRAY("byte[]"),
    CHARACTER("java.lang.Character"),
    SHORT("java.lang.Short"),
    BYTE("java.lang.Byte"),
    LIST("java.util.List"),
    SET("java.util.Set"),
    CUSTOM("java.lang.Object");

    @JsonProperty("literal")
    public String getName() {
        return this.name();
    }

    @Getter
    @JsonProperty("type")
    private final String typeClass;

    private static final Map<String, KnownTypes> of = Arrays.stream(KnownTypes.values())
                    .collect(Collectors.toMap(it -> it.typeClass, it -> it));

    KnownTypes(String typeClass) {
        this.typeClass = typeClass;
    }

    @JsonCreator
    public static KnownTypes fromJson(Object value) {
        if (value instanceof String s)
            return KnownTypes.valueOf(s.toUpperCase());
        else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) value;

            var literal = map.get("literal");
            return KnownTypes.valueOf(literal.toUpperCase());
        }

        throw new IllegalArgumentException("Unsupported value type for KnownTypes: " + value);
    }

    public static Optional<KnownTypes> of(String name) {
        return Optional.ofNullable(of.get(name));
    }
}
