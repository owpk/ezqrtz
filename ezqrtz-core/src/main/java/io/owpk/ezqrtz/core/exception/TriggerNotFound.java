package io.owpk.ezqrtz.core.exception;

public final class TriggerNotFound extends EzSchedulingException {
    public TriggerNotFound(String triggerId) {
        super("Trigger with id " + triggerId + " not found");
    }
}
