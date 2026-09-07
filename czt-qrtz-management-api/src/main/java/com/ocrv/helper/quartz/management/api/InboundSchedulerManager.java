package com.ocrv.helper.quartz.management.api;

import com.ocrv.helper.quartz.management.api.model.RemoteAdapterInfo;
import com.ocrv.helper.quartz.management.api.model.SchedulerStatus;
import com.ocrv.helper.quartz.management.api.model.SchedulerType;

/**
 * Адаптер обертка для внешнего контроля над внутренним щедулером
 *
 * @author Vorobyev Vyacheslav
 */
public interface InboundSchedulerManager extends SchedulerManager {
    RemoteAdapterInfo getInfo();

    default RemoteAdapterInfo buildInfo(String desc, SchedulerStatus status, String name, String identity) {
        return RemoteAdapterInfo.builder()
                .description(desc)
                .name(name)
                .identity(identity)
                .type(SchedulerType.QUARTZ)
                .status(status)
                .build();
    }

    default RemoteAdapterInfo buildInfo(String desc, String name, String identity) {
        return buildInfo(desc, SchedulerStatus.STARTED, name, identity);
    }
}
