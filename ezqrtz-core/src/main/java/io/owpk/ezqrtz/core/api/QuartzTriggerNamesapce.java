package io.owpk.ezqrtz.core.api;

import org.jspecify.annotations.NullMarked;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * Фабрика namespace для создания ключей триггеров и заданий Quartz.
 *
 * @author Vyacheslav Vorobev
 */
@NullMarked
public interface QuartzTriggerNamesapce {

    /**
     * Создаёт ключ триггера для указанного идентификатора.
     *
     * @param identity идентификатор триггера
     * @return созданный ключ триггера
     */
    TriggerKey triggerKey(String identity);

    /**
     * Создаёт ключ задания для указанного идентификатора.
     *
     * @param identity идентификатор задания
     * @return созданный ключ задания
     */
    JobKey jobKey(String identity);
}
