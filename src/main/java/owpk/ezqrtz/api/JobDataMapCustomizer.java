package owpk.ezqrtz.api;

import org.quartz.JobDataMap;

/**
 * Настраивает {@link org.quartz.JobDataMap} Quartz с использованием функционального интерфейса.
 *
 * @author Vyacheslav Vorobev
 */
@FunctionalInterface
public interface JobDataMapCustomizer {

    /**
     * Настраивает указанную карту данных задания.
     *
     * @param jobDataMap карта данных задания для настройки
     */
    void customize(JobDataMap jobDataMap);

    /**
     * Возвращает составной кастомизатор, который сначала применяет этот кастомизатор, затем указанный after.
     *
     * @param after кастомизатор, который применяется после этого
     * @return составной кастомизатор
     */
    default JobDataMapCustomizer andThen(JobDataMapCustomizer after) {
        return jobDataMap -> {
            this.customize(jobDataMap);
            after.customize(jobDataMap);
        };
    }
}
