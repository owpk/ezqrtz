package io.owpk.ezqrtz.management.api;

/**
 * Интерфейс для управления шедулером
 *
 * @author Vorobyev Vyacheslav
 */
public interface SchedulerManager
        extends
        JobQueryService,
        TriggerQueryService,
        TriggerControlService {

}