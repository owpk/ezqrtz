package io.owpk.ezqrtz.management.api.rest.ex;

public class EzRemoteSchedulerOperationException extends RuntimeException {

    public EzRemoteSchedulerOperationException() {
    }

    public EzRemoteSchedulerOperationException(String message) {
        super(message);
    }

    public EzRemoteSchedulerOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EzRemoteSchedulerOperationException(Throwable cause) {
        super(cause);
    }
}
