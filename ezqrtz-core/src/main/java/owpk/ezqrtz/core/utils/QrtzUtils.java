package owpk.ezqrtz.core.utils;

import owpk.ezqrtz.core.api.CztQuartzScheduleExecutor;
import owpk.ezqrtz.core.model.ScheduleResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public final class QrtzUtils {

    public static void logScheduleResult(ScheduleResult result, CztQuartzScheduleExecutor executor) {
        log.info("Scheduler executor: {}, scheduled: {}, next fire time: {}, collision strategy: {}",
                executor.getClass().getSimpleName(),
                result.scheduled(),
                result.nextFireTime(),
                result.appliedStrategy());

    }
}