package io.owpk.ezqrtz.core.api;

import io.owpk.ezqrtz.core.exception.EzSchedulingException;
import io.owpk.ezqrtz.core.model.ScheduleResult;
import org.jspecify.annotations.NullMarked;

/**
 * Исполнитель для планирования и перепланирования заданий.
 *
 * @param <T> тип запроса на планирование
 * @author Vyacheslav Vorobev
 */
@NullMarked
public interface ScheduleExecutor<T> {

    /**
     * Планирует новое задание на основе данного запроса.
     *
     * @param request запрос на планирование
     * @return результат операции планирования
     */
    ScheduleResult schedule(T request) throws EzSchedulingException;

    /**
     * Перепланирует существующее задание на основе данного запроса.
     *
     * @param request запрос на перепланирование
     * @return результат операции перепланирования
     */
    ScheduleResult reschedule(T request) throws EzSchedulingException;
}
