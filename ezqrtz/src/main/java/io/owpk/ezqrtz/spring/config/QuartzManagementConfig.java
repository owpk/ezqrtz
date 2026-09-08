package io.owpk.ezqrtz.spring.config;

import io.owpk.ezqrtz.management.api.InternalAdapterProps;
import io.owpk.ezqrtz.management.api.rest.client.SchedulingManagementRestAdapterV1Impl;
import io.owpk.ezqrtz.management.api.InboundSchedulerManager;
import io.owpk.ezqrtz.management.core.adapter.DefaultInboundSchedulerManagementAdapter;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzManagementConfig {

    @Bean
    @ConditionalOnBean(Scheduler.class)
    public InboundSchedulerManager defaultAdapter(
            Scheduler scheduler,
            InternalAdapterProps internalAdapterProps) {
        return new DefaultInboundSchedulerManagementAdapter(scheduler, internalAdapterProps);
    }

    @Bean
    @ConditionalOnMissingBean
    SchedulingManagementRestAdapterV1Impl schedulingManagementRestAdapterV1Impl(
            InboundSchedulerManager inboundSchedulerManager) {
        return new SchedulingManagementRestAdapterV1Impl(inboundSchedulerManager);
    }
}
