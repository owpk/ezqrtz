package io.opwk.ezqrtz;

import io.owpk.ezqrtz.core.annotations.EzCronJob;

import java.lang.invoke.MethodHandle;

public record EzQuartzJobRegistration(Object bean,
                                      EzCronJob annotation,
                                      MethodHandle methodHandle) {
}