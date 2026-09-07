package com.ocrv.helper.quartz.management.api.rest.ex;

public class CztRemoteSchedulerOperationException extends RuntimeException {

    public CztRemoteSchedulerOperationException() {
    }

    public CztRemoteSchedulerOperationException(String message) {
        super(message);
    }

    public CztRemoteSchedulerOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CztRemoteSchedulerOperationException(Throwable cause) {
        super(cause);
    }
}
