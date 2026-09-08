package io.owpk.ezqrtz.core.api;

import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Настраивает определения {@link Trigger} Quartz с использованием builder-паттерна.
 *
 * @param <T> тип {@link TriggerBuilder}, который настраивается
 * @author Vyacheslav Vorobev
 */
public interface TriggerCustomizer<T extends TriggerBuilder<Trigger>> {

    /**
     * Настраивает указанный builder триггера с помощью маппера ключа триггера.
     *
     * @param triggerKeyMapper функция для создания {@link TriggerKey} из имени триггера
     * @param triggerBuilder   builder триггера для настройки
     */
    void customize(Function<String, TriggerKey> triggerKeyMapper, T triggerBuilder);

    /**
     * Настраивает builder триггера с использованием фиксированной группы триггеров.
     *
     * @param triggerGroup   имя группы для триггера
     * @param triggerBuilder builder триггера для настройки
     */
    default void customize(String triggerGroup, T triggerBuilder) {
        customize(triggerName -> new TriggerKey(triggerName, triggerGroup), triggerBuilder);
    }

    /**
     * Пытается получить существующий триггер из планировщика с использованием указанного маппера ключей и группы.
     *
     * @param triggerKeyMapper функция для создания {@link TriggerKey}
     * @param triggerGroup     группа триггеров (используется как входные данные для маппера ключей)
     * @param fb               фабрика bean-компонентов планировщика
     * @param exceptionHandler обработчик для любых исключений при поиске
     * @return {@link Optional}, содержащий триггер, если найден, или пустой, если нет
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
