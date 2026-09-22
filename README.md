[![Maven Central](https://img.shields.io/maven-central/v/io.github.owpk/ezqrtz)](https://central.sonatype.com/artifact/io.github.owpk/ezqrtz)

[🇷🇺 Русская версия](README_RU.md)

# Easy Quartz 

Spring library for convenient work with the **Quartz Scheduler**. It provides a DSL for programmatic scheduling, declarative job annotations, automatic configuration of a persistent Quartz store with a C3P0 connection pool, and a REST API for scheduler management.

## Core Concept

The library addresses four typical Quartz pain points:

1. **Complexity of creating jobs and triggers** — instead of long builder chains, a single `ScheduleRequest` is enough.
2. **Manual bean registration and maintenance** — the `@CztCronJob` annotation automatically scans, registers, and schedules jobs at application startup.
3. **Persistent store configuration** — `QuartzConfig` automatically creates a DataSource based on `quartz.properties`, configures the C3P0 pool, transactions, and Spring integration.
4. **Runtime job management** — the REST API (`@EnableCztQuartzSchedulerManagement`) allows viewing, creating, updating, starting, and stopping triggers without restarting the application.

## Installation

The library is published to Maven Central. Add the `ezqrtz` starter dependency — it includes the core DSL and the management API.

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
implementation 'io.github.owpk:ezqrtz:$ezqrtzLatestVersion'
```

Individual modules (`ezqrtz-core`, `ezqrtz-management-api`, `ezqrtz-management`) can also be connected separately — see [Module Overview](#module-overview).

## Quick Start

Add `@EnableCztQuartzScheduler` to a configuration class — this is enough for scheduling:

```java
@EnableCztQuartzScheduler
@Configuration
public class AppConfig { }
```

To additionally enable scheduler management via the REST API, add `@EnableCztQuartzSchedulerManagement`:

```java
@EnableCztQuartzScheduler
@EnableCztQuartzSchedulerManagement
@Configuration
public class AppConfig { }
```

Provide a `quartz.properties` file on the classpath with DataSource configuration (driver, URL, user, password, maxConnections) and standard Quartz parameters (job store, thread pool, clustering).

---

## 1. Programmatic Scheduling — ScheduleRequest DSL

`ScheduleRequest` is a builder object that encapsulates the entire job and trigger configuration. Pass it to a `CztQuartzScheduleExecutor` — and the library will create the job, the trigger, and schedule the execution.

### Wiring the Executor

The library does not register the executor automatically — create a `CztQuartzScheduleExecutor` bean based on `DefaultCztQuartzScheduleExecutor`:

```java
@Bean
public CztQuartzScheduleExecutor scheduleExecutor(Scheduler scheduler) {
    return new DefaultCztQuartzScheduleExecutor(scheduler, List.of());
}
```

Optionally, a Quartz group namespace (`SchedulerNamespace`) can be provided to isolate job/trigger groups.

### Example

```java
@Autowired
private CztQuartzScheduleExecutor executor;

ScheduleRequest request = ScheduleRequest.builder()
        .jobClass(MyJob.class)
        .jobIdentity("my-job")
        .triggerIdentity("my-trigger")
        .description("Daily report export")
        .durable(true)
        .collisionStrategy(CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS)
        .jobDataCustomizer(map -> map.put("param", "value"))
        .trigger(CronTriggerDefinition.of("0 0 12 * * ?", "Europe/Moscow"))
        .build();

ScheduleResult result = executor.schedule(request);

// reschedule an existing trigger with new parameters
ScheduleResult rescheduled = executor.reschedule(request);
```

### Under the Hood

`DefaultCztQuartzScheduleExecutor`, based on `ScheduleRequest`:
- creates a `JobDetail` (with description, JobDataMap, durability and recovery flags);
- creates a `Trigger` (Cron / Once / Repeat) with customizers applied;
- handles collisions via `CollisionStrategy` (SKIP, FAIL, REMOVE, REPLACE_AND_RESCHEDULE_IF_EXISTS, etc.);
- invokes `SchedulerInterceptor` before and after operations.

Besides `schedule` / `reschedule`, `CztQuartzScheduleExecutor` provides management operations: `pauseJob` / `resumeJob`, `pauseTrigger` / `resumeTrigger`, `deleteJob`, `jobExists` / `triggerExists`, `getJob` / `getTrigger`.

### Customization

```java
ScheduleRequest request = ScheduleRequest.builder()
        .jobCustomizer(builder -> builder.withDescription("custom"))
        .triggerCustomizer(builder -> builder.forJob("my-job"))
        .build();
```

### Trigger Types

| Type | Class | Description |
|------|-------|-------------|
| Cron | `CronTriggerDefinition` | Cron schedule with timezone support |
| One-shot | `OnceTriggerDefinition` | Fires at a specific point in time |
| Repeating | `RepeatTriggerDefinition` | Interval + number of repetitions |

---

## 2. Declarative Scheduling — @CztCronJob

The `@CztCronJob` annotation turns a Spring bean into a schedulable task. The library automatically scans annotated classes, registers them in the job registry, and schedules them at application startup. The annotation is meta-annotated with `@Component`, so the class becomes a Spring bean automatically.

### Example

```java
@CztCronJob(
        name = "daily-report",
        group = "reports",
        cron = "0 0 12 * * ?",
        zoneId = "Europe/Moscow",
        description = "Daily report export",
        collisionStrategy = CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS
)
public class DailyReportJob {

    @Execute
    public void execute(JobExecutionContext context) {
        // job logic
    }
}
```

### Annotation Arguments

| Argument | Required | Description |
|----------|:--------:|-------------|
| `name` | ✅ | Job and trigger identifier |
| `group` | ✅ | Quartz group for isolation |
| `cron` | ✅ | Cron expression |
| `description` | ✅ | Job description |
| `zoneId` | | Time zone (defaults to the system one) |
| `collisionStrategy` | | Collision handling strategy |
| `enabled` | | Enables/disables job registration |

### How It Works

1. `QuartzBeanPostProcessor` finds beans annotated with `@CztCronJob` and their `@Execute` method.
2. `CztQuartzJobRegistrar` registers them, builds a `ScheduleRequest` from the annotation attributes, and schedules the jobs via `DefaultCztQuartzScheduleExecutor`.
3. `QuartzStartup` schedules all registered jobs once the context is up; execution is delegated to your method via `SpringJobBridge`.

---

## 3. Automatic Quartz + Database Configuration

`QuartzConfig` fully automates Quartz configuration when `quartz.properties` is present on the classpath.

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

### What Is Configured Automatically

- **C3P0 DataSource** — a connection pool is created based on the parameters from `quartz.properties`.
- **SchedulerFactoryBean** — configured with the DataSource, transactions (PlatformTransactionManager), Spring integration (AutowiringSpringBeanJobFactory), global listeners, and shutdown flags.
- **Spring integration** — `@Autowired` works inside `Job.execute()`.
- **Database schema** — `QuartzSchemaInitializer` checks whether the schema exists via JDBC metadata (`QuartzSchemaDetector`) and creates it from an SQL script if needed.
- **Logging** — scheduler information (name, ID, job store, thread pool, clustering, version) is printed at startup. The format can be customized by providing your own `BootstrapLogger` bean.

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `czt.quartz.config.path` | `quartz.properties` | Path to the Quartz configuration file on the classpath |
| `ez.quartz.schema.initialize` | `true` | Automatic database schema initialization |
| `ez.quartz.schema.script` | `quartz-init.sql` | Path to the schema SQL script on the classpath |

Schema initialization runs only when a JDBC job store is configured and only if the schema does not exist yet.

### Database Schema

The schema is created automatically: `QuartzSchemaDetector` checks its presence via JDBC metadata, and if it is missing, `QuartzSchemaInitializer` applies the SQL script (by default `quartz-init.sql` on the classpath).

---

## 4. Scheduler Management via REST API

The `@EnableCztQuartzSchedulerManagement` annotation wires in a ready-to-use REST controller (`InboundManagementController`) for managing jobs and triggers at runtime. All endpoints are available under the base path `/v1/scheduling/management`.

### Integration Annotations

| Annotation | What It Imports | Purpose |
|------------|-----------------|---------|
| `@EnableCztQuartzScheduler` | `QuartzConfig` | Automatic configuration of Quartz, DataSource, and scheduling |
| `@EnableCztQuartzSchedulerManagement` | `InboundManagementController`, `QuartzManagementConfig`, `SchedulerManagementAdvice` | REST API for scheduler management |

`QuartzManagementConfig` registers a `DescriableSchedulerManager` (`DefaultDescriableQuartzAdapter` — an adapter over `Scheduler`) through which the REST controller talks to Quartz. Management can be enabled independently of scheduling: `@EnableCztQuartzSchedulerManagement` works without `@EnableCztQuartzScheduler` as long as a `Scheduler` bean exists in the context.

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/job?id=` | Job with its current JobDataMap |
| GET | `/job/definition?id=` | Job definition (class and job data types) |
| GET | `/job/definitions` | List of registered jobs |
| GET | `/trigger?id=` | Trigger by identifier |
| GET | `/triggers` | Trigger search with filters: `id`, `description`, `group`, `name`, `cronExpression`, `nextFireTimeFrom`, `nextFireTimeTo` |
| GET | `/triggers/groups` | List of trigger groups |
| POST | `/trigger` | Create a trigger (optional `startAt`, `endAt`) |
| PUT | `/trigger` | Update a trigger (optional `startAt`, `endAt`) |
| PUT | `/trigger/start?id=` | Start (resume) a trigger |
| PUT | `/trigger/stop?id=` | Stop (pause) a trigger |
| GET | `/info` | Information about the adapter and the scheduler |

### Examples

Creating a trigger:

```http
POST /v1/scheduling/management/trigger?startAt=2026-09-07T12:00:00
Content-Type: application/json

{
  "id": "my-trigger",
  "jobId": "my-job",
  "cronExpression": "0 0 12 * * ?",
  "description": "Daily report export",
  "jobData": {
    "param": { "type": "string", "value": "value" }
  }
}
```

Stopping a trigger:

```http
PUT /v1/scheduling/management/trigger/stop?id=my-trigger
```

Response of modification operations (`TriggerModifiedResult`):

```json
{
  "id": "my-trigger",
  "success": true,
  "message": "stop"
}
```

### Error Handling

`SchedulerManagementAdvice` maps the sealed `CztSchedulerManagementException` hierarchy to appropriate HTTP statuses: `AdapterNotFound` / `JobNotFound` / `TriggerNotFound` → `404`, scheduler operation errors (`SchedulerOperation`, `RemoteSchedulerOperation`) → `500`. The response body is an `ApiError` with an error code (`RemoteErrorCode`) and a message:

```json
{
  "code": "TRIGGER_NOT_FOUND",
  "message": "Trigger 'my-trigger' not found"
}
```

### Managing Remote Schedulers

The `ezqrtz-management-api` module contains the client-side part for managing external applications with the management API connected:

- `OutboundManagementAdapter` — contract for managing remote schedulers (each operation takes an `adapterId`);
- `SMRestClient` / `DefaultSMRestClient` — REST client: builds endpoint URLs from `baseUrl`, HTTP calls are delegated to pluggable transport providers, so the client is not tied to a specific HTTP stack;
- `RemoteSchedulerRegistry` / `RegisteredClientAdapter` / `RemoteSchedulerProps` — registry of registered remote adapters and their properties (`identity`, `baseUrl`, `friendlyName`).

This allows a single service (for example, an admin panel) to manage triggers of multiple applications.

### Module Overview

| Module | Purpose |
|--------|---------|
| `ezqrtz-core` | Core: `ScheduleRequest` DSL, executors, collision strategies |
| `ezqrtz` | Spring integration: `QuartzConfig`, `@EnableCztQuartzScheduler` / `@EnableCztQuartzSchedulerManagement` annotations, `@CztCronJob` |
| `ezqrtz-management-api` | Management API: models, DTOs, REST contracts, REST client |
| `ezqrtz-management` | Implementation: REST controller, Quartz adapters, error handling |

---

## Collision Handling Strategies for Registered Jobs

| Type | Behavior |
|------|----------|
| `FAIL` | Throws `JobCollisionException` |
| `SKIP` | Skips scheduling if the job already exists |
| `REMOVE` | Removes the existing job and creates a new one |
| `SKIP_AND_REPLACE_JOB_DATA` | Skips scheduling but updates the JobDataMap |
| `REPLACE_AND_RESCHEDULE_IF_EXISTS` | Reschedules the existing trigger with new parameters (default) |
