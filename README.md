# backend-quartz-starter

Spring-библиотека для удобной работы с **Quartz Scheduler**. Предоставляет DSL для программируемого планирования, декларативные аннотации для задания и автоматическую настройку persistent-хранилища Quartz с C3P0-пулом соединений.

## Основная концепция

Библиотека закрывает три типичные боли Quartz:

1. **Сложность создания заданий и триггеров** — вместо многострочных builder-цепочек достаточно одного `ScheduleRequest`.
2. **Ручная регистрация и поддержка bean'ов** — аннотация `@ezCronJob` автоматически сканирует, регистрирует и планирует задания при старте приложения.
3. **Настройка persistent-хранилища** — `QuartzConfig` автоматически создаёт DataSource на основе `quartz.properties`, настраивает C3P0-пул, транзакции и Spring-интеграцию.

## Быстрый старт

Добавьте `@EnableezQuartzScheduler` на конфигурационный класс:

```java
@EnableezQuartzScheduler
@Configuration
public class AppConfig { }
```

Подключите `quartz.properties` в classpath с конфигурацией DataSource (driver, URL, user, password, maxConnections) и стандартных параметров Quartz (job store, thread pool, clustering).

---

## 1. Программируемое планирование — ScheduleRequest DSL

`ScheduleRequest` — builder-объект, инкапсулирующий всю конфигурацию задания и триггера. Передайте его в `ScheduleExecutor` — и библиотека создаст job, trigger и запланирует запуск.

### Пример

```java
@Autowired
private ScheduleExecutor<ScheduleRequest> executor;

ScheduleRequest request = ScheduleRequest.builder()
        .jobClass(MyJob.class)
        .jobIdentity("my-job")
        .triggerIdentity("my-trigger")
        .description("Ежедневная выгрузка отчётов")
        .durable(true)
        .collisionStrategy(CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS)
        .jobDataCustomizer(map -> map.put("param", "value"))
        .trigger(CronTriggerDefinition.of("0 0 12 * * ?", "Europe/Moscow"))
        .build();

ScheduleResult result = executor.schedule(request);
```

### Что под капотом

`DefaultezQuartzScheduleExecutor` на основе `ScheduleRequest`:
- создаёт `JobDetail` (с описанием, JobDataMap, флагами durability и recovery);
- создаёт `Trigger` (Cron / Once / Repeat) с применением кастомизаторов;
- обрабатывает коллизии через `CollisionStrategy` (SKIP, FAIL, REMOVE, REPLACE_AND_RESCHEDULE_IF_EXISTS и др.);
- вызывает `SchedulerInterceptor` до и после операций.

### Кастомизация

```java
ScheduleRequest request = ScheduleRequest.builder()
        .jobCustomizer(builder -> builder.withDescription("custom"))
        .triggerCustomizer(builder -> builder.forJob("my-job"))
        .build();
```

### Типы триггеров

| Тип | Класс | Описание |
|-----|-------|----------|
| Cron | `CronTriggerDefinition` | Cron-расписание с поддержкой timezone |
| Однократный | `OnceTriggerDefinition` | Запуск в конкретный момент времени |
| Повторяющийся | `RepeatTriggerDefinition` | Интервал + количество повторений |

---

## 2. Декларативное планирование — @ezCronJob

Аннотация `@ezCronJob` превращает Spring-bean в планируемую задачу. Библиотека автоматически сканирует marked-классы, регистрирует их в job-реестре и планирует при старте приложения.

### Пример

```java
import owpk.ezqrtz.annotations.CronTriggerJob;
import owpk.ezqrtz.annotations.Execute;

@CronTriggerJob(
        name = "daily-report",
        group = "reports",
        cron = "0 0 12 * * ?",
        zoneId = "Europe/Moscow",
        description = "Ежедневная выгрузка отчётов",
        collisionStrategy = CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS,
)
public class DailyReportJob {

    @Execute
    public void execute(JobExecutionContext context) {
        // логика задания
    }
}
```

### Аргументы аннотации

| Аргумент | Обязательный | Описание |
|----------|:-----------:|----------|
| `name` | ✅ | Идентификатор задания и триггера |
| `group` | ✅ | Группа Quartz для изоляции |
| `cron` | ✅ | Cron-выражение |
| `description` | ✅ | Описание задания |
| `zoneId` | | Часовой пояс (по умолчанию — системный) |
| `collisionStrategy` | | Стратегия обработки коллизий |
| `enabled` | | Вкл/выкл регистрацию задания |

### Как это работает

1. `QuartzBeanPostProcessor` сканирует контекст на bean'ы с `@ezCronJob`.
2. `ezQuartzJobRegistrar` создаёт `ScheduleRequest` из атрибутов аннотации и планирует задание через `DefaultezQuartzScheduleExecutor`.
3. Все аннотированные задания автоматически планируются при старте приложения.

---

## 3. Автоматическая настройка Quartz + БД

`QuartzConfig` полностью автоматизирует конфигурацию Quartz при наличии `quartz.properties` в classpath.

### quartz.properties

```properties
# Job Store
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
org.quartz.jobStore.isClustered=true
org.quartz.jobStore.clusterCheckinInterval=10000

# Thread Pool
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.threadCount=10

# DataSource (используется Quartz)
org.quartz.dataSource.quartzDataSource.driver=com.mysql.cj.jdbc.Driver
org.quartz.dataSource.quartzDataSource.URL=jdbc:mysql://localhost:3306/quartz
org.quartz.dataSource.quartzDataSource.user=root
org.quartz.dataSource.quartzDataSource.password=secret
org.quartz.dataSource.quartzDataSource.maxConnections=10
```

### Что настраивается автоматически

- **C3P0 DataSource** — создаётся пул соединений на основе параметров из `quartz.properties`.
- **SchedulerFactoryBean** — конфигурируется с DataSource, транзакциями (PlatformTransactionManager), Spring-интеграцией (AutowiringSpringBeanJobFactory), глобальными слушателями и флагами shutdown.
- **Spring-интеграция** — `@Autowired` работает внутри `Job.execute()`.
- **Логирование** — при старте выводится информация о планировщике (имя, ID, job store, thread pool, кластеризация, версия).

### Схема БД

Для persistent-режима создайте таблицы Quartz в вашей БД. Скрипты доступны в дистрибутиве Quartz:

```
quartz-*.jar/org/quartz/impl/jdbcjobstore/tables_*.sql
```

Выберите `.sql`-файл, соответствующий вашей СУБД (например, `tables_mysql_innodb.sql` для MySQL).

---

## Стратегии обработки коллизий

| Тип | Поведение |
|-----|----------|
| `FAIL` | Бросает `JobCollisionException` |
| `SKIP` | Пропускает планирование, если задание уже существует |
| `REMOVE` | Удаляет существующее задание, создаёт новое |
| `SKIP_AND_REPLACE_JOB_DATA` | Пропускает, но обновляет JobDataMap |
| `REPLACE_AND_RESCHEDULE_IF_EXISTS` | Перепланирует существующий триггер новыми параметрами (по умолчанию) |