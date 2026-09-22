package io.opwk.ezqrtz;

import io.owpk.ezqrtz.core.annotations.CztCronJob;

import java.lang.invoke.MethodHandle;

public record CztQuartzJobRegistration(Object bean,
                                       CztCronJob annotation,
                                       MethodHandle methodHandle) {
}