package io.owpk.ezqrtz.management.api.rest.v1.client;

import io.owpk.ezqrtz.management.api.model.TriggerFilter;
import io.owpk.ezqrtz.management.api.rest.v1.InboundManagementAdapter;
import io.owpk.ezqrtz.management.api.model.TriggerDef;

import java.util.List;

public interface EzSMRestClient extends InboundManagementAdapter {

    List<TriggerDef> listTriggers(TriggerFilter filter);
}
