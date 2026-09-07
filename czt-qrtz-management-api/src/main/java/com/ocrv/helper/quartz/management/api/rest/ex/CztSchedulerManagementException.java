package com.ocrv.helper.quartz.management.api.rest.ex;

/**
 * Базовое исключение управления шедулерами.
 * Абстрактный намеренно: инстанцируются только конкретные подтипы,
 * что позволяет писать исчерпывающие switch по иерархии без default-ветки.
 */
public abstract sealed class CztSchedulerManagementException extends RuntimeException
        permits
        AdapterNotFoundException,
        SchedulerOperationException,
        JobNotFound,
        TriggerNotFound {

    protected CztSchedulerManagementException(String message) {
        super(message);
    }

    protected CztSchedulerManagementException(Throwable e) {
        super(e);
    }
}
