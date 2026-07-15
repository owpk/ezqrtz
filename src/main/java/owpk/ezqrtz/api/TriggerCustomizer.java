package owpk.ezqrtz.api;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Configures Quartz {@link Trigger} definitions using the builder pattern.
 *
 * @param <T> the type of {@link TriggerBuilder} being configured
 * @author Vyacheslav Vorobev
 */
public interface TriggerCustomizer<T extends TriggerBuilder<Trigger>> {

    /**
     * Configures the specified trigger builder using a trigger key mapper.
     *
     * @param triggerKeyMapper function for creating a {@link TriggerKey} from a trigger name
     * @param triggerBuilder   trigger builder to configure
     */
    void customize(Function<String, TriggerKey> triggerKeyMapper, T triggerBuilder);

    /**
     * Configures the trigger builder using a fixed trigger group.
     *
     * @param triggerGroup   name of the group for the trigger
     * @param triggerBuilder trigger builder to configure
     */
    default void customize(String triggerGroup, T triggerBuilder) {
        customize(triggerName -> new TriggerKey(triggerName, triggerGroup), triggerBuilder);
    }

    /**
     * Attempts to retrieve an existing trigger from the scheduler using the provided key mapper and group.
     *
     * @param triggerKeyMapper function for creating a {@link TriggerKey}
     * @param triggerGroup     trigger group (used as input for the key mapper)
     * @param fb               scheduler bean factory
     * @param exceptionHandler handler for any exceptions that occur during lookup
     * @return {@link Optional} containing the trigger if found, or empty otherwise
     */
    default Optional<Trigger> customize(Function<String, TriggerKey> triggerKeyMapper, String triggerGroup, SchedulerFactoryBean fb,
                                        Consumer<Throwable> exceptionHandler) {
        var key = triggerKeyMapper.apply(triggerGroup);
        try {
            var trigger = fb.getScheduler().getTrigger(key);
            return Optional.ofNullable(trigger);
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
        return Optional.empty();
    }
}
