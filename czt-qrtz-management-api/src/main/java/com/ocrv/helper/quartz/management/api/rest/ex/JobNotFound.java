package com.ocrv.helper.quartz.management.api.rest.ex;

public final class JobNotFound extends CztSchedulerManagementException {
    public JobNotFound(String message) {
        super(message);
    }
}
