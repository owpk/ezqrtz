package io.owpk.ezqrtz.management.api;

import io.owpk.ezqrtz.management.api.model.JobDef;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Builder
public record InternalAdapterProps(
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

    public InternalAdapterProps(String adapterId, String adapterDesc) {
        this(adapterId, adapterDesc, new JobInfoRegistry());
    }

    @SafeVarargs
    public InternalAdapterProps(String adapterId, String adapterDesc,
                                JobDef<Map<String, Class<?>>> ... jobsInfo) {
        this(adapterId, adapterDesc, new JobInfoRegistry(Stream.of(jobsInfo).toList()));
    }

    public InternalAdapterProps(String adapterId, String adapterDesc, List<JobDef<Map<String, Class<?>>>> jobsInfo) {
        this(adapterId, adapterDesc, new JobInfoRegistry(jobsInfo));
    }
}
