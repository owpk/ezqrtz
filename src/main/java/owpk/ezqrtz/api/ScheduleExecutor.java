package owpk.ezqrtz.api;

import owpk.ezqrtz.internal.model.ScheduleResult;

/**
 * Executor for scheduling and rescheduling jobs.
 *
 * @param <T> the type of scheduling request
 * @author Vyacheslav Vorobev
 */
public interface ScheduleExecutor<T> {

    /**
     * Schedules a new job based on the given request.
     *
     * @param request the scheduling request
     * @return the result of the scheduling operation
     */
    ScheduleResult schedule(T request);

    /**
     * Reschedules an existing job based on the given request.
     *
     * @param request the rescheduling request
     * @return the result of the rescheduling operation
     */
    ScheduleResult reschedule(T request);
}
