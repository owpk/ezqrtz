package io.owpk.ezqrtz.management.api.ex;

/**
 * Базовое исключение управления шедулерами.
 */
public sealed class EzSchedulerManagementException extends RuntimeException
        permits
        AdapterNotFound,
        SchedulerOperation,
        RemoteSchedulerOperation,
        JobNotFound,
        TriggerNotFound {

    public EzSchedulerManagementException(String message) {
        super(message);
    }

    public EzSchedulerManagementException(Throwable e) {
        super(e);
    }
}