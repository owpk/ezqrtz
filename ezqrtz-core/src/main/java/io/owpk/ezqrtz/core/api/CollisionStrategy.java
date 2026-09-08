package io.owpk.ezqrtz.core.api;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.function.Supplier;

/**
 * Стратегия обработки коллизий заданий и триггеров в планировщике.
 *
 * @author Vyacheslav Vorobev
 */
public interface CollisionStrategy {

    /**
     * Обрабатывает коллизию между существующим и новым заданием/триггером.
     *
     * @param scheduler        планировщик Quartz
     * @param jobKey           ключ конфликтующего задания
     * @param jobDetailFactory фабрика для создания нового описания задания
     * @param triggerFactory   фабрика для создания нового триггера
     * @return true, если коллизия обработана, false в противном случае
     */
    boolean handle(
            Scheduler scheduler,
            JobKey jobKey,
            Supplier<JobDetail> jobDetailFactory,
            Supplier<Trigger> triggerFactory
    ) throws SchedulerException;

}