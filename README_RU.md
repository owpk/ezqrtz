[![Maven Central](https://img.shields.io/maven-central/v/io.github.owpk/ezqrtz)](https://central.sonatype.com/artifact/io.github.owpk/ezqrtz)

[🇬🇧 English version](README.md)

# Easy Quartz 

Spring-библиотека для удобной работы с **Quartz Scheduler**. Предоставляет DSL для программируемого планирования, декларативные аннотации для задания, автоматическую настройку persistent-хранилища Quartz с C3P0-пулом соединений и REST API для управления шедулерами.

## Основная концепция

Библиотека закрывает четыре типичные боли Quartz:

1. **Сложность создания заданий и триггеров** — вместо многострочных builder-цепочек достаточно одного `ScheduleRequest`.
2. **Ручная регистрация и поддержка bean'ов** — аннотация `@EzCronJob` автоматически сканирует, регистрирует и планирует задания при старте приложения.
3. **Настройка persistent-хранилища** — `QuartzConfig` автоматически создаёт DataSource на основе `quartz.properties`, настраивает C3P0-пул, транзакции и Spring-интеграцию.
4. **Управление заданиями в рантайме** — REST API (`@EnableEzQuartzSchedulerManagement`) позволяет просматривать, создавать, обновлять, запускать и останавливать триггеры без перезапуска приложения.

## Установка

Библиотека опубликована в Maven Central. Добавьте зависимость `ezqrtz` — она включает ядро с DSL и management API.

**Maven:**

```xml
<dependency>
    <groupId>io.github.owpk</groupId>
    <artifactId>ezqrtz</artifactId>
    <version>${ezqrtz.latest.version}</version>
</dependency>
```

**Gradle (Groovy DSL):**

```groovy
implementation `io.github.owpk:ezqrtz:$ezqrtzLatestVersion`
```

Отдельные модули (`ezqrtz-core`, `ezqrtz-management-api`, `ezqrtz-management`) также можно подключать по отдельности — см. [Состав модулей](#состав-модулей).

## Быстрый старт

Добавьте `@EnableEzQuartzScheduler` на конфигурационный класс — этого достаточно для планирования:

```java
@EnableEzQuartzScheduler
@Configuration
public class AppConfig { }
```

Чтобы дополнительно включить управление шедулерами через REST API, добавьте `@EnableEzQuartzSchedulerManagement`:

```java
@EnableEzQuartzScheduler
@EnableEzQuartzSchedulerManagement
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

`DefaultEzQuartzScheduleExecutor` на основе `ScheduleRequest`:
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

## 2. Декларативное планирование — @EzCronJob

Аннотация `@EzCronJob` превращает Spring-bean в планируемую задачу. Библиотека автоматически сканирует marked-классы, регистрирует их в job-реестре и планирует при старте приложения.

### Пример

```java
@EzCronJob(
        name = "daily-report",
        group = "reports",
        cron = "0 0 12 * * ?",
        zoneId = "Europe/Moscow",
        description = "Ежедневная выгрузка отчётов",
        collisionStrategy = CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS
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

1. `QuartzBeanPostProcessor` сканирует контекст на bean'ы с `@EzCronJob`.
2. `EzQuartzJobRegistrar` создаёт `ScheduleRequest` из атрибутов аннотации и планирует задание через `DefaultEzQuartzScheduleExecutor`.
3. Все аннотированные задания автоматически планируются при старте приложения.

---

## 3. Автоматическая настройка Quartz + БД

`QuartzConfig` полностью автоматизирует конфигурацию Quartz при наличии `quartz.properties` в classpath.

### quartz.properties

```properties
# Main Scheduler Properties
org.quartz.scheduler.instanceName=ogo-domain-scheduling-cluster
org.quartz.scheduler.instanceId=AUTO

# Datasource
org.quartz.dataSource.quartzDataSource.driver=org.postgresql.Driver
org.quartz.dataSource.quartzDataSource.URL=jdbc:postgresql://${YOUR_ENV_DB_HOST:localhost}:${YOUR_ENV_DB_PORT:5432}/${YOUR_ENV_DB_NAME:db}
org.quartz.dataSource.quartzDataSource.user=${YOUR_ENV_DB_USER:postgres}
org.quartz.dataSource.quartzDataSource.password=${YOUR_ENV_DB_PASS:postgres}
org.quartz.dataSource.quartzDataSource.maxConnections=10

# Job Store
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
org.quartz.jobStore.isClustered=true
org.quartz.jobStore.clusterCheckinInterval=10000

# Thread Pool
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.threadCount=10
```

### Что настраивается автоматически

- **C3P0 DataSource** — создаётся пул соединений на основе параметров из `quartz.properties`.
- **SchedulerFactoryBean** — конфигурируется с DataSource, транзакциями (PlatformTransactionManager), Spring-интеграцией (AutowiringSpringBeanJobFactory), глобальными слушателями и флагами shutdown.
- **Spring-интеграция** — `@Autowired` работает внутри `Job.execute()`.
- **Логирование** — при старте выводится информация о планировщике (имя, ID, job store, thread pool, кластеризация, версия).

### Схема БД

Настраивается автоматически 

---

## 4. Управление шедулерами через REST API

Аннотация `@EnableEzQuartzSchedulerManagement` подключает готовый REST-контроллер (`V1SchedulingManagementController`) для управления заданиями и триггерами в рантайме. Все эндпоинты доступны по базовому пути `/v1/scheduling/management`.

### Аннотации интеграции

| Аннотация | Что импортирует | Назначение |
|-----------|-----------------|------------|
| `@EnableEzQuartzScheduler` | `QuartzConfig` | Автоматическая настройка Quartz, DataSource и планирования |
| `@EnableEzQuartzSchedulerManagement` | `V1SchedulingManagementController`, `QuartzManagementConfig` | REST API для управления шедулерами |

`QuartzManagementConfig` регистрирует `InboundSchedulerManager` (адаптер над `Scheduler`) и `SchedulingManagementRestAdapterV1Impl` — слой между REST-контроллером и Quartz. Управление можно включать независимо от планирования: `@EnableEzQuartzSchedulerManagement` работает и без `@EnableEzQuartzScheduler`, если в контексте есть bean `Scheduler`.

### Эндпоинты

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/job?id=` | Задание с текущим JobDataMap |
| GET | `/job/definition?id=` | Определение задания (класс и типы job data) |
| GET | `/job/definitions` | Список зарегистрированных заданий |
| GET | `/trigger?id=` | Триггер по идентификатору |
| GET | `/triggers` | Поиск триггеров с фильтрами: `id`, `description`, `group`, `name`, `cronExpression`, `nextFireTimeFrom`, `nextFireTimeTo` |
| GET | `/triggers/groups` | Список групп триггеров |
| POST | `/trigger` | Создание триггера (опционально `startAt`, `endAt`) |
| PUT | `/trigger` | Обновление триггера (опционально `startAt`, `endAt`) |
| PUT | `/trigger/start?id=` | Запуск (resume) триггера |
| PUT | `/trigger/stop?id=` | Остановка (pause) триггера |
| GET | `/info` | Информация об адаптере и шедулере |

### Примеры

Создание триггера:

```http
POST /v1/scheduling/management/trigger?startAt=2026-09-07T12:00:00
Content-Type: application/json

{
  "id": "my-trigger",
  "jobId": "my-job",
  "cronExpression": "0 0 12 * * ?",
  "description": "Ежедневная выгрузка отчётов",
  "jobData": {
    "param": { "type": "string", "value": "value" }
  }
}
```

Остановка триггера:

```http
PUT /v1/scheduling/management/trigger/stop?id=my-trigger
```

Ответ операций изменения (`TriggerModifiedResult`):

```json
{
  "id": "my-trigger",
  "success": true,
  "message": "Trigger successfully stopped"
}
```

### Обработка ошибок

`SchedulerManagementAdvice` маппит контролируемые исключения в корректные HTTP-статусы: `JobNotFound` / `TriggerNotFound` → `404`, остальные ошибки планировщика → `500` (`EzRemoteSchedulerOperationException`).

### Управление удалёнными шедулерами

Модуль `czt-qrtz-management-api` содержит клиентскую часть для управления внешними приложениями с подключённым management-API:

- `SchedulingManagementRestAdapterV1` — контракт REST-адаптера (v1);
- `SchedulerManagementV1RestClientHelper` — построение URL эндпоинтов по `baseUrl`;
- `SchedulingManagementOutboundAdapterV1` — outbound-адаптер с указанием `adapterId` удалённого шедулера.

Это позволяет одному сервису (например, панели администрирования) управлять триггерами нескольких приложений.

### Состав модулей

| Модуль | Назначение |
|--------|------------|
| `ezqrtz-core` | Ядро: DSL `ScheduleRequest`, исполнители, стратегии коллизий |
| `ezqrtz` | Spring-интеграция: `QuartzConfig`, аннотации `@EnableEzQuartzScheduler` / `@EnableEzQuartzSchedulerManagement`, `@EzCronJob` |
| `ezqrtz-management-api` | API управления: модели, DTO, REST-контракты, клиентские хелперы |
| `ezqrtz-management` | Реализация: REST-контроллер, адаптеры над Quartz, обработка ошибок |

---

## Стратегии обработки коллизий зарегистрированных заданий

| Тип | Поведение |
|-----|----------|
| `FAIL` | Бросает `JobCollisionException` |
| `SKIP` | Пропускает планирование, если задание уже существует |
| `REMOVE` | Удаляет существующее задание, создаёт новое |
| `SKIP_AND_REPLACE_JOB_DATA` | Пропускает, но обновляет JobDataMap |
| `REPLACE_AND_RESCHEDULE_IF_EXISTS` | Перепланирует существующий триггер новыми параметрами (по умолчанию) |
