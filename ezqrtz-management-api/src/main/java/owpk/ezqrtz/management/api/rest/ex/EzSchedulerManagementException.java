package owpk.ezqrtz.management.api.rest.ex;

/**
 * Базовое исключение управления шедулерами.
 * Абстрактный намеренно: инстанцируются только конкретные подтипы,
 * что позволяет писать исчерпывающие switch по иерархии без default-ветки.
 */
public abstract sealed class EzSchedulerManagementException extends RuntimeException
        permits
        AdapterNotFoundException,
        SchedulerOperationException,
        JobNotFound,
        TriggerNotFound {

    protected EzSchedulerManagementException(String message) {
        super(message);
    }

    protected EzSchedulerManagementException(Throwable e) {
        super(e);
    }
}
