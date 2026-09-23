package io.owpk.ezqrtz.core.exception;

public sealed class EzSchedulingException extends RuntimeException
        permits
        JobCollisionException,
        SchedulerOperationException,
        JobNotFound, TriggerNotFound {

    public EzSchedulingException(String message) {
        super(message);
    }

    public EzSchedulingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EzSchedulingException(Throwable cause) {
        super(cause);
    }
}
