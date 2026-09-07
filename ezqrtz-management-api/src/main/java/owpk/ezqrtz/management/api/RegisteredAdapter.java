package owpk.ezqrtz.management.api;

import owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1;
import owpk.ezqrtz.management.api.model.RemoteSchedulerProps;

public interface RegisteredAdapter extends SchedulingManagementRestAdapterV1 {

    RemoteSchedulerProps props();
}
