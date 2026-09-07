package owpk.ezqrtz.management.api.rest;

import owpk.ezqrtz.management.api.dto.JobDataCreateProperty;
import owpk.ezqrtz.management.api.dto.JobDataProperty;
import owpk.ezqrtz.management.api.dto.KnownTypes;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class JobDataMapper {
    private JobDataMapper() {
        /* This utility class should not be instantiated */
    }

    public static JobDataProperty mapToJobDataProperty(Object value) {
        return JobDataProperty.builder()
                .type(KnownTypes.of(value.getClass().getName())
                        .orElseThrow())
                .value(value.toString())
                .build();
    }

    private static final Map<Class<?>, Function<String, ?>> CONVERTERS = Map.of(
            String.class, Function.identity(),
            Integer.class, Integer::valueOf,
            Long.class, Long::valueOf,
            Double.class, Double::valueOf,
            Boolean.class, Boolean::valueOf,
            Float.class, Float::valueOf,
            Short.class, Short::valueOf,
            Byte.class, Byte::valueOf,
            Character.class, value -> {
                if (value.length() != 1)
                    throw new IllegalArgumentException("Expected single character");
                return value.charAt(0);
            }
    );

    public static Optional<Object> mapToObj(JobDataCreateProperty property) {
        if (property.value() == null)
            return Optional.empty();

        try {
            var value = property.value();
            var type = Class.forName(property.type().getTypeClass());

            var converter = CONVERTERS.get(type);
            if (converter != null)
                return Optional.of(converter.apply(value));

            var valueOf = type.getMethod("valueOf", String.class);
            return Optional.ofNullable(valueOf.invoke(null, value));

        } catch (ReflectiveOperationException | IllegalArgumentException _) {
            return Optional.empty();
        }
    }

}
