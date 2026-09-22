package io.owpk.ezqrtz.core.exception;

public final class TriggerNotFound extends CztSchedulingException {
    public TriggerNotFound(String triggerId) {
        super("Trigger with id " + triggerId + " not found");
    }
}
