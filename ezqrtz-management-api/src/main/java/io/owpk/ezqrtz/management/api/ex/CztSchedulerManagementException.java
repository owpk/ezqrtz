package io.owpk.ezqrtz.management.api.ex;

/**
 * Базовое исключение управления шедулерами.
 */
public sealed class CztSchedulerManagementException extends RuntimeException
        permits
        AdapterNotFound,
        SchedulerOperation,
        RemoteSchedulerOperation,
        JobNotFound,
        TriggerNotFound {

    public CztSchedulerManagementException(String message) {
        super(message);
    }

    public CztSchedulerManagementException(Throwable e) {
        super(e);
    }
}