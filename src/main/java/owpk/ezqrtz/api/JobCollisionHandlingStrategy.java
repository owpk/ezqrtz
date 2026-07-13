package owpk.ezqrtz.api;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import owpk.ezqrtz.exception.JobCollisionException;

/**
 * Strategy to handle job collisions.
 *
 * @author Vyacheslav Vorobev
 */
@FunctionalInterface
public interface JobCollisionHandlingStrategy {

    void handle(Scheduler scheduler, JobDetail jobDetail) throws JobCollisionException;
}
