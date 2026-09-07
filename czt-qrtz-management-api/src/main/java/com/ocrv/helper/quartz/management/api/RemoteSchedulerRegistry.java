package com.ocrv.helper.quartz.management.api;

import com.ocrv.helper.quartz.management.api.model.RemoteSchedulerProps;

import java.util.List;
import java.util.Optional;

public interface RemoteSchedulerRegistry {
    RegisteredAdapter registerManager(RemoteSchedulerProps props);
    List<RegisteredAdapter> list();
    Optional<RegisteredAdapter> find(String id);
    Optional<RegisteredAdapter> remove(String id);
}
