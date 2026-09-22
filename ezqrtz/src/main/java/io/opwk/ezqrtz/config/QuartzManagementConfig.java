package io.opwk.ezqrtz.config;

import io.owpk.ezqrtz.management.api.model.AdapterInfo;
import io.owpk.ezqrtz.management.api.DescriableSchedulerManager;
import io.owpk.ezqrtz.management.adapter.internal.DefaultDescriableQuartzAdapter;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzManagementConfig {

    @Bean
    @ConditionalOnBean(Scheduler.class)
    public DescriableSchedulerManager defaultAdapter(
            Scheduler scheduler,
            AdapterInfo adapterInfo) {
        return new DefaultDescriableQuartzAdapter(scheduler, adapterInfo);
    }
}
