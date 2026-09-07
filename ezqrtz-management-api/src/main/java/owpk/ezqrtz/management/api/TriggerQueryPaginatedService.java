package owpk.ezqrtz.management.api;

import owpk.ezqrtz.management.api.model.TriggerDef;
import owpk.ezqrtz.management.api.model.TriggerFilter;

import java.util.List;

public interface TriggerQueryPaginatedService {

    List<TriggerDef> listTriggers(int page, int size, TriggerFilter filter);

    default List<TriggerDef> listTriggers(int page, int size) {
        return listTriggers(page, size, TriggerFilter.empty());
    }

}
