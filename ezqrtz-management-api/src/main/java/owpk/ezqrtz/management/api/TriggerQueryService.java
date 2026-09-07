package owpk.ezqrtz.management.api;

import owpk.ezqrtz.management.api.model.Result;
import owpk.ezqrtz.management.api.model.TriggerDef;
import owpk.ezqrtz.management.api.model.TriggerFilter;

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