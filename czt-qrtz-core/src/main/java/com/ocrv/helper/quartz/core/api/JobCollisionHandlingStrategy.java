package com.ocrv.helper.quartz.core.api;

import com.ocrv.helper.quartz.core.exception.JobCollisionException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

/**
 * Strategy to handle job collisions.
 *
 * @author Vyacheslav Vorobev
 */
@FunctionalInterface
public interface JobCollisionHandlingStrategy {

    void handle(Scheduler scheduler, JobDetail jobDetail) throws JobCollisionException;
}
