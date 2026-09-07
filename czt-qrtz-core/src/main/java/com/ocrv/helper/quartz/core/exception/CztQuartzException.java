package com.ocrv.helper.quartz.core.exception;

public sealed class CztQuartzException extends RuntimeException
        permits
        JobCollisionException,
        SchedulerOperationException {

    public CztQuartzException(String message) {
        super(message);
    }

    public CztQuartzException(String message, Throwable cause) {
        super(message, cause);
    }

    public CztQuartzException(Throwable cause) {
        super(cause);
    }
}
