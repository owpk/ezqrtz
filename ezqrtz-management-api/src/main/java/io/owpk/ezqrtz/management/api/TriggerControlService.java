package io.owpk.ezqrtz.management.api;

import io.owpk.ezqrtz.management.api.model.Result;
import io.owpk.ezqrtz.management.api.model.TriggerDef;
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
