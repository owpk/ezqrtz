package io.owpk.ezqrtz.management.api.rest;

import io.owpk.ezqrtz.management.api.ex.AdapterNotFound;
import io.owpk.ezqrtz.management.api.rest.v1.InboundManagementAdapter;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public interface RemoteSchedulerRegistry {
    void registerManager(InboundManagementAdapter adapter);

    List<InboundManagementAdapter> list();

    InboundManagementAdapter find(String id) throws AdapterNotFound;

    InboundManagementAdapter remove(String id) throws AdapterNotFound;
}
