package com.ocrv.helper.quartz.management.api;

import com.ocrv.helper.quartz.management.api.model.Result;
import com.ocrv.helper.quartz.management.api.model.TriggerDef;
import com.ocrv.helper.quartz.management.api.model.TriggerFilter;

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