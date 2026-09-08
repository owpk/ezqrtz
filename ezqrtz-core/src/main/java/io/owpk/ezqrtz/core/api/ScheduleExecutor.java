package io.owpk.ezqrtz.core.api;

import io.owpk.ezqrtz.core.model.ScheduleResult;

/**
 * Исполнитель для планирования и перепланирования заданий.
 *
 * @param <T> тип запроса на планирование
 * @author Vyacheslav Vorobev
 */
public interface ScheduleExecutor<T> {

    /**
     * Планирует новое задание на основе данного запроса.
     *
     * @param request запрос на планирование
     * @return результат операции планирования
     */
    ScheduleResult schedule(T request);

    /**
     * Перепланирует существующее задание на основе данного запроса.
     *
     * @param request запрос на перепланирование
     * @return результат операции перепланирования
     */
    ScheduleResult reschedule(T request);
}
