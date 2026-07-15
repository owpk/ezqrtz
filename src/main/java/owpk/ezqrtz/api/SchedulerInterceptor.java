package owpk.ezqrtz.api;

import owpk.ezqrtz.internal.model.ScheduleRequest;

import java.time.LocalDate;

/**
 * Interceptor for scheduling and rescheduling operations.
 *
 * @author Vyacheslav Vorobev
 */
public interface SchedulerInterceptor {

    /**
     * Called before scheduling a job.
     *
     * @param request scheduling request
     */
    default void beforeSchedule(ScheduleRequest request) {
    }

    /**
     * Called after scheduling a job.
     *
     * @param request      scheduling request
     * @param nextFireTime the next fire time of the scheduled job
     */
    default void afterSchedule(ScheduleRequest request, LocalDate nextFireTime) {
    }

    /**
     * Called before rescheduling a job.
     *
     * @param request rescheduling request
     */
    default void beforeReschedule(ScheduleRequest request) {
    }

    /**
     * Called after rescheduling a job.
     *
     * @param request      rescheduling request
     * @param nextFireTime the next fire time of the rescheduled job
     */
    default void afterReschedule(ScheduleRequest request, LocalDate nextFireTime) {
    }
}