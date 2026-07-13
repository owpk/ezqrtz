package owpk.ezqrtz.api;

import owpk.ezqrtz.internal.model.ScheduleRequest;

import java.time.LocalDate;

/**
 * Перехватчик для операций планирования и перепланирования.
 *
 * @author Vyacheslav Vorobev
 */
public interface SchedulerInterceptor {

    /**
     * Вызывается перед планированием задания.
     *
     * @param request запрос на планирование
     */
    default void beforeSchedule(ScheduleRequest request) {
    }

    /**
     * Вызывается после планирования задания.
     *
     * @param request      запрос на планирование
     * @param nextFireTime время следующего срабатывания запланированного задания
     */
    default void afterSchedule(ScheduleRequest request, LocalDate nextFireTime) {
    }

    /**
     * Вызывается перед перепланированием задания.
     *
     * @param request запрос на перепланирование
     */
    default void beforeReschedule(ScheduleRequest request) {
    }

    /**
     * Вызывается после перепланирования задания.
     *
     * @param request      запрос на перепланирование
     * @param nextFireTime время следующего срабатывания перепланированного задания
     */
    default void afterReschedule(ScheduleRequest request, LocalDate nextFireTime) {
    }
}