package io.owpk.ezqrtz.management.api.ex;

import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public final class JobNotFound extends CztSchedulerManagementException {
    public JobNotFound(String jobId) {
        super("Job not found: " + jobId);
    }

    public JobNotFound(Supplier<String> msgSupplier) {
        super(msgSupplier.get());
    }
}
