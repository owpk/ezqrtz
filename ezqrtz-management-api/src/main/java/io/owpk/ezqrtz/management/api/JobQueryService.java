package io.owpk.ezqrtz.management.api;

import io.owpk.ezqrtz.management.api.ex.JobNotFound;
import io.owpk.ezqrtz.management.api.model.JobDef;
import io.owpk.ezqrtz.management.api.model.Result;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;

@NullMarked
public interface JobQueryService {

    List<JobDef<Map<String, Class<?>>>> listJobs();

    JobDef<Map<String, Class<?>>> getJobDefinition(String id) throws JobNotFound;

    Result<JobDef<Map<String, Object>>> getJob(String id);
}