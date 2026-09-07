package com.ocrv.helper.quartz.management.api;

import com.ocrv.helper.quartz.management.api.rest.client.SchedulingManagementRestAdapterV1;
import com.ocrv.helper.quartz.management.api.model.RemoteSchedulerProps;

public interface RegisteredAdapter extends SchedulingManagementRestAdapterV1 {

    RemoteSchedulerProps props();
}
