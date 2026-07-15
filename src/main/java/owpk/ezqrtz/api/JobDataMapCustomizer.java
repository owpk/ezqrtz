package owpk.ezqrtz.api;

import org.quartz.JobDataMap;

/**
 * Customizes {@link org.quartz.JobDataMap} Quartz using a functional interface.
 *
 * @author Vyacheslav Vorobev
 */
@FunctionalInterface
public interface JobDataMapCustomizer {

    /**
     * Customizes the specified job data map.
     *
     * @param jobDataMap the job data map to customize
     */
    void customize(JobDataMap jobDataMap);

    /**
     * Returns a composed customizer that first applies this customizer, then the specified after customizer.
     *
     * @param after the customizer to apply after this one
     * @return a composed customizer
     */
    default JobDataMapCustomizer andThen(JobDataMapCustomizer after) {
        return jobDataMap -> {
            this.customize(jobDataMap);
            after.customize(jobDataMap);
        };
    }
}
