package owpk.ezqrtz.spring;

import owpk.ezqrtz.core.annotations.CztCronJob;

import java.lang.invoke.MethodHandle;

public record CztQuartzJobRegistration(Object bean,
                                       CztCronJob annotation,
                                       MethodHandle methodHandle) {
}