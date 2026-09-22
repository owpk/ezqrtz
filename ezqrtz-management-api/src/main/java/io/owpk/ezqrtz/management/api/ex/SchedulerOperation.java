package io.owpk.ezqrtz.management.api.ex;

public final class SchedulerOperation extends CztSchedulerManagementException {

    public SchedulerOperation(String message) {
        super(message);
    }

    public SchedulerOperation(Throwable e) {
        super(e);
    }
}
