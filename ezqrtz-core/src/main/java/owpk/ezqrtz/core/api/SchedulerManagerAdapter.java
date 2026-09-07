package owpk.ezqrtz.core.api;

/**
 * Адаптер для общих операций планировщика над заданиями и триггерами.
 *
 * @author Vyacheslav Vorobev
 */
public interface SchedulerManagerAdapter {

    /**
     * Проверяет, существует ли задание или триггер с указанным идентификатором.
     *
     * @param identity идентификатор задания или триггера
     * @return true, если существует, false в противном случае
     */
    boolean exists(String identity);

    /**
     * Удаляет задание и триггер с указанным идентификатором.
     *
     * @param identity идентификатор задания или триггера
     * @return true, если успешно удалено, false в противном случае
     */
    boolean delete(String identity);

    /**
     * Активирует (запускает) задание с указанным идентификатором.
     *
     * @param identity идентификатор задания
     * @return true, если успешно активировано, false в противном случае
     */
    boolean trigger(String identity);

    /**
     * Ставит на паузу задание или триггер с указанным идентификатором.
     *
     * @param identity идентификатор задания или триггера
     * @return true, если успешно поставлено на паузу, false в противном случае
     */
    boolean pause(String identity);

    /**
     * Снимает с паузы задание или триггер с указанным идентификатором.
     *
     * @param identity идентификатор задания или триггера
     * @return true, если успешно снято с паузы, false в противном случае
     */
    boolean resume(String identity);
}
