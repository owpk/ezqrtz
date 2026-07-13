package owpk.ezqrtz.spring;

import owpk.ezqrtz.annotations.CronTriggerJob;

import java.lang.invoke.MethodHandle;

public record EzQuartzJobRegistration(Object bean,
                                      CronTriggerJob annotation,
                                      MethodHandle methodHandle) {
}