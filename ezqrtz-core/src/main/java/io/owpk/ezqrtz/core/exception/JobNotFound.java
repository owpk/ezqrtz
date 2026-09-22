package io.owpk.ezqrtz.core.exception;

public final class JobNotFound extends CztSchedulingException {
    public JobNotFound(String jobId) {
        super("Job not found: " + jobId);
    }
}
