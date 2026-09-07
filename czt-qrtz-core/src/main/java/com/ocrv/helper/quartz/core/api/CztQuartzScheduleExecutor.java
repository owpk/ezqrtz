package com.ocrv.helper.quartz.core.api;

import com.ocrv.helper.quartz.core.model.ScheduleRequest;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Расширенный исполнитель планирования Quartz с операциями управления заданиями и триггерами.
 *
 * @author Vyacheslav Vorobev
 */
public interface CztQuartzScheduleExecutor extends ScheduleExecutor<ScheduleRequest> {

    /**
     * Возвращает базовый планировщик Quartz.
     *
     * @return планировщик Quartz
     */
    Scheduler getScheduler();

    /**
     * Ставит на паузу задание с указанным идентификатором.
     *
     * @param identity идентификатор задания
     * @return true, если успешно поставлено на паузу, false в противном случае
     */
    boolean pauseJob(String identity);

    /**
     * Ставит на паузу триггер с указанным идентификатором.
     *
     * @param identity идентификатор триггера
     * @return true, если успешно поставлено на паузу, false в противном случае
     */
    boolean pauseTrigger(String identity);

    /**
     * Снимает с паузы задание с указанным идентификатором.
     *
     * @param identity идентификатор задания
     * @return true, если успешно снято с паузы, false в противном случае
     */
    boolean resumeJob(String identity);

    /**
     * Снимает с паузы триггер с указанным идентификатором.
     *
     * @param identity идентификатор триггера
     * @return true, если успешно снято с паузы, false в противном случае
     */
    boolean resumeTrigger(String identity);

    /**
     * Удаляет задание с указанным идентификатором.
     *
     * @param identity идентификатор задания
     * @return true, если успешно удалено, false в противном случае
     */
    boolean deleteJob(String identity);

    /**
     * Проверяет, существует ли задание с указанным идентификатором.
     *
     * @param identity идентификатор задания
     * @return true, если существует, false в противном случае
     */
    boolean jobExists(String identity);

    /**
     * Проверяет, существует ли триггер с указанным идентификатором.
     *
     * @param identity идентификатор триггера
     * @return true, если существует, false в противном случае
     */
    boolean triggerExists(String identity);

    /**
     * Ставит на паузу задание с указанным идентификатором, обрабатывая ошибки через колбэк.
     *
     * @param identity идентификатор задания
     * @param onError  колбэк, вызываемый при ошибке
     */
    void pauseJob(String identity, Consumer<Exception> onError);

    /**
     * Ставит на паузу триггер с указанным идентификатором, обрабатывая ошибки через колбэк.
     *
     * @param identity идентификатор триггера
     * @param onError  колбэк, вызываемый при ошибке
     */
    void pauseTrigger(String identity, Consumer<Exception> onError);

    /**
     * Снимает с паузы задание с указанным идентификатором, обрабатывая ошибки через колбэк.
     *
     * @param identity идентификатор задания
     * @param onError  колбэк, вызываемый при ошибке
     */
    void resumeJob(String identity, Consumer<Exception> onError);

    /**
     * Снимает с паузы триггер с указанным идентификатором, обрабатывая ошибки через колбэк.
     *
     * @param identity идентификатор триггера
     * @param onError  колбэк, вызываемый при ошибке
     */
    void resumeTrigger(String identity, Consumer<Exception> onError);

    /**
     * Удаляет задание с указанным идентификатором, обрабатывая ошибки через колбэк.
     *
     * @param identity идентификатор задания
     * @param onError  колбэк, вызываемый при ошибке
     */
    void deleteJob(String identity, Consumer<Exception> onError);

    /**
     * Возвращает триггер с указанным идентификатором.
     *
     * @param identity идентификатор триггера
     * @return Optional, содержащий триггер, если найден
     */
    Optional<Trigger> getTrigger(String identity);

    /**
     * Возвращает ключ триггера для указанного идентификатора.
     *
     * @param identity идентификатор триггера
     * @return ключ триггера
     */
    TriggerKey getTriggerKey(String identity);

    /**
     * Возвращает описание задания с указанным идентификатором.
     *
     * @param identity идентификатор задания
     * @return Optional, содержащий описание задания, если найдено
     */
    Optional<JobDetail> getJob(String identity);

    /**
     * Возвращает ключ задания для указанного идентификатора.
     *
     * @param identity идентификатор задания
     * @return ключ задания
     */
    JobKey getJobKey(String identity);
}
