package owpk.ezqrtz.management.api;

import owpk.ezqrtz.management.api.model.JobDef;
import owpk.ezqrtz.management.api.model.Result;

import java.util.List;
import java.util.Map;

public interface JobQueryService {

    List<JobDef<Map<String, Class<?>>>> listJobs();

    JobDef<Map<String, Class<?>>> getJobDefinition(String id);

    Result<JobDef<Map<String, Object>>> getJob(String id);
}
