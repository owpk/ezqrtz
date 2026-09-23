package io.owpk.ezqrtz.management.api.rest;

import io.owpk.ezqrtz.management.api.rest.v1.client.EzSMRestClient;
import io.owpk.ezqrtz.management.api.model.RemoteSchedulerProps;

public interface RegisteredClientAdapterEz extends EzSMRestClient {

    RemoteSchedulerProps props();
}
