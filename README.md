[🇷🇺 Русская версия](README_RU.md)

# Easy Quartz 

Spring library for convenient work with the **Quartz Scheduler**. It provides a DSL for programmatic scheduling, declarative job annotations, automatic configuration of a persistent Quartz store with a C3P0 connection pool, and a REST API for scheduler management.

## Core Concept

The library addresses four typical Quartz pain points:

1. **Complexity of creating jobs and triggers** — instead of long builder chains, a single `ScheduleRequest` is enough.
2. **Manual bean registration and maintenance** — the `@EzCronJob` annotation automatically scans, registers, and schedules jobs at application startup.
3. **Persistent store configuration** — `QuartzConfig` automatically creates a DataSource based on `quartz.properties`, configures the C3P0 pool, transactions, and Spring integration.
4. **Runtime job management** — the REST API (`@EnableEzQuartzSchedulerManagement`) allows viewing, creating, updating, starting, and stopping triggers without restarting the application.

## Quick Start

Add `@EnableEzQuartzScheduler` to a configuration class — this is enough for scheduling:

```java
@EnableEzQuartzScheduler
@Configuration
public class AppConfig { }
```

To additionally enable scheduler management via the REST API, add `@EnableEzQuartzSchedulerManagement`:

```java
@EnableEzQuartzScheduler
@EnableEzQuartzSchedulerManagement
@Configuration
public class AppConfig { }
```

Provide a `quartz.properties` file on the classpath with DataSource configuration (driver, URL, user, password, maxConnections) and standard Quartz parameters (job store, thread pool, clustering).

---

## 1. Programmatic Scheduling — ScheduleRequest DSL

`ScheduleRequest` is a builder object that encapsulates the entire job and trigger configuration. Pass it to `ScheduleExecutor` — and the library will create the job, the trigger, and schedule the execution.

### Example

```java
@Autowired
private ScheduleExecutor<ScheduleRequest> executor;

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
```

### Under the Hood

`DefaultEzQuartzScheduleExecutor`, based on `ScheduleRequest`:
- creates a `JobDetail` (with description, JobDataMap, durability and recovery flags);
- creates a `Trigger` (Cron / Once / Repeat) with customizers applied;
- handles collisions via `CollisionStrategy` (SKIP, FAIL, REMOVE, REPLACE_AND_RESCHEDULE_IF_EXISTS, etc.);
- invokes `SchedulerInterceptor` before and after operations.

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

## 2. Declarative Scheduling — @EzCronJob

The `@EzCronJob` annotation turns a Spring bean into a schedulable task. The library automatically scans annotated classes, registers them in the job registry, and schedules them at application startup.

### Example

```java
@EzCronJob(
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

1. `QuartzBeanPostProcessor` scans the context for beans annotated with `@EzCronJob`.
2. `EzQuartzJobRegistrar` builds a `ScheduleRequest` from the annotation attributes and schedules the job via `DefaultEzQuartzScheduleExecutor`.
3. All annotated jobs are automatically scheduled at application startup.

---

## 3. Automatic Quartz + Database Configuration

`QuartzConfig` fully automates Quartz configuration when `quartz.properties` is present on the classpath.

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

# DataSource (used by Quartz)
org.quartz.dataSource.quartzDataSource.driver=com.mysql.cj.jdbc.Driver
org.quartz.dataSource.quartzDataSource.URL=jdbc:mysql://localhost:3306/quartz
org.quartz.dataSource.quartzDataSource.user=root
org.quartz.dataSource.quartzDataSource.password=secret
org.quartz.dataSource.quartzDataSource.maxConnections=10
```

### What Is Configured Automatically

- **C3P0 DataSource** — a connection pool is created based on the parameters from `quartz.properties`.
- **SchedulerFactoryBean** — configured with the DataSource, transactions (PlatformTransactionManager), Spring integration (AutowiringSpringBeanJobFactory), global listeners, and shutdown flags.
- **Spring integration** — `@Autowired` works inside `Job.execute()`.
- **Logging** — scheduler information (name, ID, job store, thread pool, clustering, version) is printed at startup.

### Database Schema

Configured automatically.

---

## 4. Scheduler Management via REST API

The `@EnableEzQuartzSchedulerManagement` annotation wires in a ready-to-use REST controller (`V1SchedulingManagementController`) for managing jobs and triggers at runtime. All endpoints are available under the base path `/v1/scheduling/management`.

### Integration Annotations

| Annotation | What It Imports | Purpose |
|------------|-----------------|---------|
| `@EnableEzQuartzScheduler` | `QuartzConfig` | Automatic configuration of Quartz, DataSource, and scheduling |
| `@EnableEzQuartzSchedulerManagement` | `V1SchedulingManagementController`, `QuartzManagementConfig` | REST API for scheduler management |

`QuartzManagementConfig` registers `InboundSchedulerManager` (an adapter over `Scheduler`) and `SchedulingManagementRestAdapterV1Impl` — a layer between the REST controller and Quartz. Management can be enabled independently of scheduling: `@EnableEzQuartzSchedulerManagement` works without `@EnableEzQuartzScheduler` as long as a `Scheduler` bean exists in the context.

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
  "message": "Trigger successfully stopped"
}
```

### Error Handling

`SchedulerManagementAdvice` maps controlled exceptions to appropriate HTTP statuses: `JobNotFound` / `TriggerNotFound` → `404`, other scheduler errors → `500` (`EzRemoteSchedulerOperationException`).

### Managing Remote Schedulers

The `czt-qrtz-management-api` module contains the client-side part for managing external applications with the management API connected:

- `SchedulingManagementRestAdapterV1` — REST adapter contract (v1);
- `SchedulerManagementV1RestClientHelper` — builds endpoint URLs from `baseUrl`;
- `SchedulingManagementOutboundAdapterV1` — outbound adapter specifying the `adapterId` of a remote scheduler.

This allows a single service (for example, an admin panel) to manage triggers of multiple applications.

### Module Overview

| Module | Purpose |
|--------|---------|
| `ezqrtz-core` | Core: `ScheduleRequest` DSL, executors, collision strategies |
| `ezqrtz` | Spring integration: `QuartzConfig`, `@EnableEzQuartzScheduler` / `@EnableEzQuartzSchedulerManagement` annotations, `@EzCronJob` |
| `ezqrtz-management-api` | Management API: models, DTOs, REST contracts, client helpers |
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
