package io.opwk.ezqrtz;

import org.springframework.beans.factory.SmartInitializingSingleton;

public record QuartzStartup(CztQuartzJobRegistrar registrar) implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        registrar.initAll();
    }
}