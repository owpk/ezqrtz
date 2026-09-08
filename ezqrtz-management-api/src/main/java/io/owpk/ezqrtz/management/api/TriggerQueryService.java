package io.owpk.ezqrtz.management.api;

import io.owpk.ezqrtz.management.api.model.Result;
import io.owpk.ezqrtz.management.api.model.TriggerDef;
import io.owpk.ezqrtz.management.api.model.TriggerFilter;

import java.util.List;
import java.util.Set;

public interface TriggerQueryService {

    Set<String> listTriggerGroups();

    Result<TriggerDef> getTrigger(String id);

    List<TriggerDef> listTriggers(TriggerFilter filter);

    default  List<TriggerDef> listTriggers() {
        return listTriggers(TriggerFilter.empty());
    }

}