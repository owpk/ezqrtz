package io.opwk.ezqrtz;

import org.springframework.beans.factory.SmartInitializingSingleton;

public record QuartzStartup(EzQuartzJobRegistrar registrar) implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        registrar.initAll();
    }
}