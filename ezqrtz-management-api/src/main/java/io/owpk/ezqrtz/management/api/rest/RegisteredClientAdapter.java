package io.owpk.ezqrtz.management.api.rest;

import io.owpk.ezqrtz.management.api.rest.v1.client.SMRestClient;
import io.owpk.ezqrtz.management.api.model.RemoteSchedulerProps;

public interface RegisteredClientAdapter extends SMRestClient {

    RemoteSchedulerProps props();
}
