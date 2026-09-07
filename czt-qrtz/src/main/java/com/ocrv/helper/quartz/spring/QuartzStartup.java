package com.ocrv.helper.quartz.spring;

import org.springframework.beans.factory.SmartInitializingSingleton;

public record QuartzStartup(CztQuartzJobRegistrar registrar) implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        registrar.initAll();
    }
}