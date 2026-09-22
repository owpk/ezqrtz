package io.owpk.ezqrtz.management.api.ex;

import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public final class AdapterNotFound extends CztSchedulerManagementException {

    public AdapterNotFound(String adapterId) {
        super("Adapter with id '" + adapterId + "' not found");
    }

    public AdapterNotFound(Supplier<String> msgSupplier) {
        super(msgSupplier.get());
    }
}
