package owpk.ezqrtz.api;

import org.quartz.*;

import java.util.function.Supplier;

/**
 * Strategy for handling job and trigger collisions in the scheduler.
 *
 * @author Vyacheslav Vorobev
 */
public interface CollisionStrategy {

    /**
     * Handles a collision between an existing and a new job/trigger.
     *
     * @param scheduler        the Quartz scheduler
     * @param jobKey           the key of the conflicting job
     * @param jobDetailFactory factory for creating a new job detail
     * @param triggerFactory   factory for creating a new trigger
     * @return true if the collision was handled, false otherwise
     */
    boolean handle(
            Scheduler scheduler,
            JobKey jobKey,
            Supplier<JobDetail> jobDetailFactory,
            Supplier<Trigger> triggerFactory
    ) throws SchedulerException;

}