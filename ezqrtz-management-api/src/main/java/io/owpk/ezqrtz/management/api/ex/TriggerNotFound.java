package io.owpk.ezqrtz.management.api.ex;

import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public final class TriggerNotFound extends EzSchedulerManagementException {
    public TriggerNotFound(String message) {
        super(message);
    }

    public TriggerNotFound(Supplier<String> msgSupplier) {
        super(msgSupplier.get());
    }
}
