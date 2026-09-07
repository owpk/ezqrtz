package owpk.ezqrtz.management.api;

/**
 * Интерфейс для управления внутренним шедулером
 *
 * @author Vorobyev Vyacheslav
 */
public interface SchedulerManager
        extends
        JobQueryService,
        TriggerQueryService,
        TriggerControlService {

}