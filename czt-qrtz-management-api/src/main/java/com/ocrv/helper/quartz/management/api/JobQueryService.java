package com.ocrv.helper.quartz.management.api;

import com.ocrv.helper.quartz.management.api.model.JobDef;
import com.ocrv.helper.quartz.management.api.model.Result;

import java.util.List;
import java.util.Map;

public interface JobQueryService {

    List<JobDef<Map<String, Class<?>>>> listJobs();

    JobDef<Map<String, Class<?>>> getJobDefinition(String id);

    Result<JobDef<Map<String, Object>>> getJob(String id);
}
