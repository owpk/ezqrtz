package io.owpk.ezqrtz.management.api;

import io.owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1;
import io.owpk.ezqrtz.management.api.model.RemoteSchedulerProps;

public interface RegisteredAdapter extends SchedulingManagementRestAdapterV1 {

    RemoteSchedulerProps props();
}
