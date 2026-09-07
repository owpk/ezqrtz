package owpk.ezqrtz.spring;

import org.springframework.beans.factory.SmartInitializingSingleton;

public record QuartzStartup(EzQuartzJobRegistrar registrar) implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        registrar.initAll();
    }
}