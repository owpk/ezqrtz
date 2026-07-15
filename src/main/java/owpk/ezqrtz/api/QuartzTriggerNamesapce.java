package owpk.ezqrtz.api;

import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * Namespace factory for creating Quartz trigger and job keys.
 *
 * @author Vyacheslav Vorobev
 */
public interface QuartzTriggerNamesapce {

    /**
     * Creates a trigger key for the given identity.
     *
     * @param identity trigger identity
     * @return the created trigger key
     */
    TriggerKey triggerKey(String identity);

    /**
     * Creates a job key for the given identity.
     *
     * @param identity job identity
     * @return the created job key
     */
    JobKey jobKey(String identity);
}
