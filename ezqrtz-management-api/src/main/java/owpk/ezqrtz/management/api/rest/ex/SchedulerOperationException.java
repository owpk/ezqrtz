package owpk.ezqrtz.management.api.rest.ex;

public final class SchedulerOperationException extends EzSchedulerManagementException {

    public SchedulerOperationException(String message) {
        super(message);
    }

    public SchedulerOperationException(Throwable e) {
        super(e);
    }
}
