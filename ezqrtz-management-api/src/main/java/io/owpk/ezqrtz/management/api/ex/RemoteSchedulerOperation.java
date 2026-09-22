package io.owpk.ezqrtz.management.api.ex;

public final class RemoteSchedulerOperation extends CztSchedulerManagementException {

    public RemoteSchedulerOperation(String message) {
        super(message);
    }

    public RemoteSchedulerOperation(Throwable cause) {
        super(cause);
    }
}
