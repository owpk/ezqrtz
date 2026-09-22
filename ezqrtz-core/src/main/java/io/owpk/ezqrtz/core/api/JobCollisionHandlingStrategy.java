package io.owpk.ezqrtz.core.api;

import io.owpk.ezqrtz.core.exception.JobCollisionException;
import org.jspecify.annotations.NullMarked;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

/**
 * Strategy to handle job collisions.
 *
 * @author Vyacheslav Vorobev
 */
@FunctionalInterface
@NullMarked
public interface JobCollisionHandlingStrategy {

    void handle(Scheduler scheduler, JobDetail jobDetail) throws JobCollisionException;
}
