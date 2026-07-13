package owpk.ezqrtz.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.Optional;

@Slf4j
public class JobsListenerService implements JobListener {

    @Override
    public String getName() {
        return "JobsListenerService";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        log.info("Job to be executed: {}", context.getJobDetail().getKey().getName());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        log.info("Job execution vetoed: {}", context.getJobDetail().getKey().getName());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        var msg = Optional.ofNullable(jobException).map(ex ->
                " with exception: " + ex.getMessage()).orElse("");
        log.info("Job was executed: {}, {}", msg, context.getJobDetail().getKey().getName());
    }

}
