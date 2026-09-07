package com.ocrv.helper.quartz.management.api;

import com.ocrv.helper.quartz.management.api.model.Result;
import com.ocrv.helper.quartz.management.api.model.TriggerDef;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public interface TriggerControlService {

    Result<Boolean> createTrigger(TriggerDef def,
                                  @Nullable LocalDateTime start,
                                  @Nullable  LocalDateTime end);

    Result<Boolean> updateTrigger(TriggerDef def,
                                  @Nullable LocalDateTime start,
                                  @Nullable  LocalDateTime end);

    Result<Boolean> stopTrigger(String id);

    Result<Boolean> startTrigger(String id);
}
