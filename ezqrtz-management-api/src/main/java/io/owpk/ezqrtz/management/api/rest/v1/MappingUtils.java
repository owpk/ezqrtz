package io.owpk.ezqrtz.management.api.rest.v1;

import io.owpk.ezqrtz.management.api.model.JobDef;
import io.owpk.ezqrtz.management.api.model.TriggerDef;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDataCreateProperty;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDataProperty;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.JobDefInfoDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.KnownTypes;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefCreateDto;
import io.owpk.ezqrtz.management.api.rest.v1.dto.TriggerDefUpdateDto;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MappingUtils {
    private MappingUtils() {
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


    public static TriggerDef mapToTriggerDef(TriggerDefCreateDto def) {
        var trigger = TriggerDef.builder()
                .cronExpression(def.cronExpression())
                .description(def.description())
                .id(def.id())
                .jobId(def.jobId());

        if (def.jobData() != null)
            trigger.jobData(def.jobData().entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            it -> MappingUtils.mapToObj(it.getValue())
                                    .orElseThrow(() ->
                                            new IllegalArgumentException("Cannot deserialize property: " + it.getValue())))));

        return trigger.build();
    }

    public static TriggerDef mapToTriggerDef(TriggerDefUpdateDto def) {
        var trigger = TriggerDef.builder()
                .cronExpression(def.cronExpression())
                .description(def.description())
                .id(def.id());

        return trigger.build();
    }

    public static JobDefDto mapJob(JobDef<Map<String, Object>> job) {
        var jobData = job.data().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        it -> MappingUtils.mapToJobDataProperty(it.getValue())));

        return JobDefDto.builder()
                .jobClass(job.jobClass().getName())
                .jobDataMap(jobData)
                .description(job.description())
                .id(job.id())
                .build();
    }

    public static JobDefInfoDto mapJobDef(JobDef<Map<String, Class<?>>> job) {
        return JobDefInfoDto.builder()
                .jobClass(job.jobClass().getName())
                .jobDataDefinition(job.data().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                it -> KnownTypes.of(it.getValue().getName())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Unsupported job data type: " + it.getValue().getName())))))
                .description(job.description())
                .id(job.id())
                .build();
    }
}