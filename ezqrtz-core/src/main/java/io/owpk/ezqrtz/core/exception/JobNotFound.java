package io.owpk.ezqrtz.core.exception;

public final class JobNotFound extends EzSchedulingException {
    public JobNotFound(String jobId) {
        super("Job not found: " + jobId);
    }
}
