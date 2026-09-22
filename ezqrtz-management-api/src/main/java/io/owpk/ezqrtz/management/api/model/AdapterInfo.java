package io.owpk.ezqrtz.management.api.model;

import io.owpk.ezqrtz.management.api.JobInfoRegistry;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Builder
public record AdapterInfo(

        /*
         * Идентификатор адаптера
         */
        String adapterId,

        /*
         * Описание адаптера
         */
        String adapterDesc,

        /*
         * Контейнер зарегистрированных задач
         */
        JobInfoRegistry jobInfoRegistry
) {

    public AdapterInfo {
        if (adapterDesc == null)
            adapterDesc = "";
        if (jobInfoRegistry == null)
            jobInfoRegistry = new JobInfoRegistry();
    }

    public AdapterInfo(String adapterId, String adapterDesc) {
        this(adapterId, adapterDesc, new JobInfoRegistry());
    }

    @SafeVarargs
    public AdapterInfo(String adapterId, String adapterDesc,
                       JobDef<Map<String, Class<?>>> ... jobsInfo) {
        this(adapterId, adapterDesc, new JobInfoRegistry(Stream.of(jobsInfo).toList()));
    }

    public AdapterInfo(String adapterId, String adapterDesc, List<JobDef<Map<String, Class<?>>>> jobsInfo) {
        this(adapterId, adapterDesc, new JobInfoRegistry(jobsInfo));
    }
}