package io.owpk.ezqrtz.core.api;

import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder для пар ключ-значение карты данных задания Quartz.
 *
 * @author Vyacheslav Vorobev
 */
@Builder(toBuilder = true)
public record JobDataBuilder(
        Map<String, Object> values
) {

    /**
     * Конструктор по умолчанию, создающий пустой builder карты данных задания.
     */
    public JobDataBuilder() {
        this(new HashMap<>());
    }

    /**
     * Добавляет пару ключ-значение в карту данных задания.
     *
     * @param key   ключ
     * @param value значение
     * @return данный builder для цепочки вызовов
     */
    public JobDataBuilder put(String key, Object value) {
        values.put(key, value);
        return this;
    }
}