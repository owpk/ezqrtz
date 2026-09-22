package io.owpk.ezqrtz.core.exception;

public sealed class CztSchedulingException extends RuntimeException
        permits
        JobCollisionException,
        SchedulerOperationException,
        JobNotFound, TriggerNotFound {

    public CztSchedulingException(String message) {
        super(message);
    }

    public CztSchedulingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CztSchedulingException(Throwable cause) {
        super(cause);
    }
}
