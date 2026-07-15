package owpk.ezqrtz.api;

import org.quartz.*;
import owpk.ezqrtz.internal.model.ScheduleRequest;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Advanced Quartz scheduling executor with job and trigger management operations.
 *
 * @author Vyacheslav Vorobev
 */
public interface EzQuartzScheduleExecutor extends ScheduleExecutor<ScheduleRequest> {

    /**
     * Returns the underlying Quartz scheduler.
     *
     * @return the Quartz scheduler
     */
    Scheduler getScheduler();

    /**
     * Pauses the job with the given identity.
     *
     * @param identity job identity
     * @return true if successfully paused, false otherwise
     */
    boolean pauseJob(String identity);

    /**
     * Pauses the trigger with the given identity.
     *
     * @param identity trigger identity
     * @return true if successfully paused, false otherwise
     */
    boolean pauseTrigger(String identity);

    /**
     * Resumes the job with the given identity.
     *
     * @param identity job identity
     * @return true if successfully resumed, false otherwise
     */
    boolean resumeJob(String identity);

    /**
     * Resumes the trigger with the given identity.
     *
     * @param identity trigger identity
     * @return true if successfully resumed, false otherwise
     */
    boolean resumeTrigger(String identity);

    /**
     * Deletes the job with the given identity.
     *
     * @param identity job identity
     * @return true if successfully deleted, false otherwise
     */
    boolean deleteJob(String identity);

    /**
     * Checks if a job with the given identity exists.
     *
     * @param identity job identity
     * @return true if exists, false otherwise
     */
    boolean jobExists(String identity);

    /**
     * Checks if a trigger with the given identity exists.
     *
     * @param identity trigger identity
     * @return true if exists, false otherwise
     */
    boolean triggerExists(String identity);

    /**
     * Pauses the job with the given identity, handling errors via callback.
     *
     * @param identity job identity
     * @param onError  error callback
     */
    void pauseJob(String identity, Consumer<Exception> onError);

    /**
     * Pauses the trigger with the given identity, handling errors via callback.
     *
     * @param identity trigger identity
     * @param onError  error callback
     */
    void pauseTrigger(String identity, Consumer<Exception> onError);

    /**
     * Resumes the job with the given identity, handling errors via callback.
     *
     * @param identity job identity
     * @param onError  error callback
     */
    void resumeJob(String identity, Consumer<Exception> onError);

    /**
     * Resumes the trigger with the given identity, handling errors via callback.
     *
     * @param identity trigger identity
     * @param onError  error callback
     */
    void resumeTrigger(String identity, Consumer<Exception> onError);

    /**
     * Deletes the job with the given identity, handling errors via callback.
     *
     * @param identity job identity
     * @param onError  error callback
     */
    void deleteJob(String identity, Consumer<Exception> onError);

    /**
     * Returns the trigger with the given identity.
     *
     * @param identity trigger identity
     * @return Optional containing the trigger if found
     */
    Optional<Trigger> getTrigger(String identity);

    /**
     * Returns the trigger key for the given identity.
     *
     * @param identity trigger identity
     * @return the trigger key
     */
    TriggerKey getTriggerKey(String identity);

    /**
     * Returns the job description with the given identity.
     *
     * @param identity job identity
     * @return Optional containing the job description if found
     */
    Optional<JobDetail> getJob(String identity);

    /**
     * Returns the job key for the given identity.
     *
     * @param identity job identity
     * @return the job key
     */
    JobKey getJobKey(String identity);
}
