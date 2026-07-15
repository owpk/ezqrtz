package owpk.ezqrtz.api;

/**
 * Adapter for common scheduler operations on jobs and triggers.
 *
 * @author Vyacheslav Vorobev
 */
public interface SchedulerManagerAdapter {

    /**
     * Checks whether a job or trigger with the specified identity exists.
     *
     * @param identity job or trigger identity
     * @return true if it exists, false otherwise
     */
    boolean exists(String identity);

    /**
     * Deletes the job and trigger with the specified identity.
     *
     * @param identity job or trigger identity
     * @return true if deleted successfully, false otherwise
     */
    boolean delete(String identity);

    /**
     * Triggers (starts) the job with the specified identity.
     *
     * @param identity job identity
     * @return true if triggered successfully, false otherwise
     */
    boolean trigger(String identity);

    /**
     * Pauses the job or trigger with the specified identity.
     *
     * @param identity job or trigger identity
     * @return true if paused successfully, false otherwise
     */
    boolean pause(String identity);

    /**
     * Resumes the job or trigger with the specified identity.
     *
     * @param identity job or trigger identity
     * @return true if resumed successfully, false otherwise
     */
    boolean resume(String identity);
}
