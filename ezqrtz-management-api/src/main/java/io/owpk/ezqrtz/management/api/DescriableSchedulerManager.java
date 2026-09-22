package io.owpk.ezqrtz.management.api;

import io.owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import io.owpk.ezqrtz.management.api.model.SchedulerStatus;
import io.owpk.ezqrtz.management.api.model.SchedulerType;

/**
 * Адаптер для предоставлении информации о шедулере
 *
 * @author Vorobyev Vyacheslav
 */
public interface DescriableSchedulerManager extends SchedulerManager {
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