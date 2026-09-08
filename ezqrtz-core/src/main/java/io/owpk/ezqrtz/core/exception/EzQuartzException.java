package io.owpk.ezqrtz.core.exception;

public sealed class EzQuartzException extends RuntimeException
        permits
        JobCollisionException,
        SchedulerOperationException {

    public EzQuartzException(String message) {
        super(message);
    }

    public EzQuartzException(String message, Throwable cause) {
        super(message, cause);
    }

    public EzQuartzException(Throwable cause) {
        super(cause);
    }
}
