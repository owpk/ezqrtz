package com.ocrv.helper.quartz.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum JobDataMapConstants {
    TYPE("type"),
    LOGIN("current_user_login"),
    ID("id");

    private static final Map<String, JobDataMapConstants> constantsMap =
            Arrays.stream(JobDataMapConstants.values())
                    .collect(Collectors.toMap(it -> it.name, Function.identity()));
    private final String name;

    JobDataMapConstants(String name) {
        this.name = name;
    }

    public static JobDataMapConstants getByName(String name) {
        return Optional.ofNullable(constantsMap.get(name))
                .orElseThrow(() -> new RuntimeException("No job data constant found: " + name));
    }
}