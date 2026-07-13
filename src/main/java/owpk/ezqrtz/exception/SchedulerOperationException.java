package owpk.ezqrtz.exception;

public final class SchedulerOperationException extends EzQuartzException {

    public SchedulerOperationException(String msg) {
        super(msg);
    }

    public SchedulerOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulerOperationException(Throwable cause) {
        super(cause);
    }
}
