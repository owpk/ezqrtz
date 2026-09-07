package com.ocrv.helper.quartz.management.api.rest.ex;

public final class SchedulerOperationException extends CztSchedulerManagementException {

    public SchedulerOperationException(String message) {
        super(message);
    }

    public SchedulerOperationException(Throwable e) {
        super(e);
    }
}
