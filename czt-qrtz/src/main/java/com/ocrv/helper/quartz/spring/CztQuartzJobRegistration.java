package com.ocrv.helper.quartz.spring;

import com.ocrv.helper.quartz.core.annotations.CztCronJob;

import java.lang.invoke.MethodHandle;

public record CztQuartzJobRegistration(Object bean,
                                       CztCronJob annotation,
                                       MethodHandle methodHandle) {
}