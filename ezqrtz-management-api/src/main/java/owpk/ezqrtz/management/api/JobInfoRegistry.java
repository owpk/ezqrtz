package owpk.ezqrtz.management.api;

import owpk.ezqrtz.management.api.model.JobDef;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JobInfoRegistry {
    private final Map<String, JobDef<Map<String, Class<?>>>> registeredJobs;

    public JobInfoRegistry() {
        this.registeredJobs = new ConcurrentHashMap<>();
    }

    public JobInfoRegistry(List<JobDef<Map<String, Class<?>>>> jobDefs) {
        this.registeredJobs = jobDefs.stream().collect(Collectors.toMap(
                JobDef::getId, Function.identity(),
                (up, _) -> up,
                ConcurrentHashMap::new));
    }

    public Optional<JobDef<Map<String, Class<?>>>> getById(@NonNull String id) {
        return Optional.ofNullable(registeredJobs.get(id));
    }

    public void registerJob(@NonNull JobDef<Map<String, Class<?>>> jobDef) {
        registeredJobs.put(jobDef.getId(), jobDef);
    }

    /**
     * Проверяет, зарегистрирована ли задача с указанным идентификатором.
     *
     * @param id идентификатор задачи
     * @return true, если задача зарегистрирована, иначе false
     * @throws IllegalArgumentException если id равен null
     */
    public boolean containsJob(@NonNull String id) {
        return registeredJobs.containsKey(id);
    }

    /**
     * Возвращает неизменяемую коллекцию всех зарегистрированных определений задач.
     *
     * @return коллекция определений задач
     */
    public Collection<JobDef<Map<String, Class<?>>>> getAllJobs() {
        return Collections.unmodifiableCollection(registeredJobs.values());
    }

    /**
     * Возвращает количество зарегистрированных задач.
     *
     * @return количество задач
     */
    public int size() {
        return registeredJobs.size();
    }

    /**
     * Удаляет все зарегистрированные задачи.
     */
    public void clear() {
        registeredJobs.clear();
    }

}