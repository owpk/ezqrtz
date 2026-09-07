# backend-quartz-starter

A Spring library for convenient work with the **Quartz Scheduler**. It provides a DSL for programmatic scheduling, declarative job annotations, and automatic configuration of Quartz persistent storage with a C3P0 connection pool.

## Core concept

The library addresses three common Quartz pain points:

1. **Complex job and trigger creation** — instead of long builder chains, a single `ScheduleRequest` is enough.
2. **Manual bean registration and support** — the `@ezCronJob` annotation automatically scans, registers, and schedules jobs at application startup.
3. **Persistent storage configuration** — `QuartzConfig` automatically creates a DataSource from `quartz.properties`, configures the C3P0 pool, transactions, and Spring integration.

## Publishing to GitHub Packages

This project can be published as a Maven artifact to GitHub Packages so you can consume it from local Spring Boot applications.

### What you need
- A public GitHub repository.
- A GitHub token with `read:packages` and `write:packages` permissions.
- A repository secret named `GITHUB_TOKEN` (or use the default `GITHUB_TOKEN` in Actions).

### Publish flow
1. Push the project to GitHub.
2. Create a release tag, for example `v2.0.1`.
3. GitHub Actions will publish the artifact automatically.

### Consume from another Maven project
```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/ocrv/spring-boot-starter-ez-quartz</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.ocrv.helper</groupId>
    <artifactId>spring-boot-starter-ez-quartz</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Consume from a Gradle project

For Groovy DSL:

```groovy
repositories {
    maven {
        url = uri('https://maven.pkg.github.com/ocrv/spring-boot-starter-ez-quartz')
        credentials {
            username = project.findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
            password = project.findProperty('gpr.key') ?: System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'com.ocrv.helper:spring-boot-starter-ez-quartz:2.0.1'
}
```

For Kotlin DSL:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/ocrv/spring-boot-starter-ez-quartz")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.ocrv.helper:spring-boot-starter-ez-quartz:2.0.1")
}
```

### Local publish command
```bash
./mvnw -B deploy -DskipTests
```

If you want, the next step can be to add a small `mvnw`/`settings.xml` example for local projects so the dependency is ready to copy-paste.

## Quick start

Add `@EnableEzQuartzScheduler` to a configuration class:

```java
@EnableEzQuartzScheduler
@Configuration
public class AppConfig { }
```

Place `quartz.properties` on the classpath with DataSource configuration (driver, URL, user, password, maxConnections) and standard Quartz settings (job store, thread pool, clustering).

---

## 1. Programmatic scheduling — ScheduleRequest DSL

`ScheduleRequest` is a builder object that encapsulates the entire configuration of a job and trigger. Pass it to `ScheduleExecutor`, and the library will create the job, trigger, and schedule the execution.

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

### What happens under the hood

`DefaultezQuartzScheduleExecutor` uses `ScheduleRequest` to:

- create a `JobDetail` (with description, JobDataMap, and durability/recovery flags);
- create a `Trigger` (Cron / Once / Repeat) with customizers applied;
- handle collisions through `CollisionStrategy` (SKIP, FAIL, REMOVE, REPLACE_AND_RESCHEDULE_IF_EXISTS, and others);
- invoke `SchedulerInterceptor` before and after operations.

### Customization

```java
ScheduleRequest request = ScheduleRequest.builder()
        .jobCustomizer(builder -> builder.withDescription("custom"))
        .triggerCustomizer(builder -> builder.forJob("my-job"))
        .build();
```

### Trigger types

| Type | Class | Description |
|-----|-------|-------------|
| Cron | `CronTriggerDefinition` | Cron schedule with timezone support |
| One-time | `OnceTriggerDefinition` | Runs at a specific point in time |
| Repeating | `RepeatTriggerDefinition` | Interval-based repeated execution |

---

## 2. Declarative scheduling — @ezCronJob

The `@ezCronJob` annotation turns a Spring bean into a scheduled job. The library automatically scans marked classes, registers them in the job registry, and schedules them at startup.

### Example

```java
import owpk.ezqrtz.annotations.CronTriggerJob;
import owpk.ezqrtz.annotations.Execute;

@CronTriggerJob(
        name = "daily-report",
        group = "reports",
        cron = "0 0 12 * * ?",
        zoneId = "Europe/Moscow",
        description = "Daily report export",
        collisionStrategy = CollisionStrategyType.REPLACE_AND_RESCHEDULE_IF_EXISTS,
)
public class DailyReportJob {

    @Execute
    public void execute(JobExecutionContext context) {
        // job logic
    }
}
```

### Annotation arguments

| Argument | Required | Description |
|----------|:--------:|-------------|
| `name` | ✅ | Job and trigger identifier |
| `group` | ✅ | Quartz group for isolation |
| `cron` | ✅ | Cron expression |
| `description` | ✅ | Job description |
| `zoneId` | | Time zone (defaults to the system zone) |
| `collisionStrategy` | | Collision handling strategy |
| `enabled` | | Enables or disables job registration |

### How it works

1. `QuartzBeanPostProcessor` scans the context for beans annotated with `@ezCronJob`.
2. `ezQuartzJobRegistrar` creates a `ScheduleRequest` from the annotation attributes and schedules the job via `DefaultezQuartzScheduleExecutor`.
3. All annotated jobs are automatically scheduled at startup.

---

## 3. Automatic Quartz + database setup

`QuartzConfig` fully automates Quartz configuration when `quartz.properties` is present on the classpath.

### quartz.properties

```properties
# Configure Main Scheduler Properties
org.quartz.scheduler.instanceName=my-cool-scheduling-cluster
org.quartz.scheduler.instanceId=AUTO

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

### What is configured automatically

- **C3P0 DataSource** — creates a connection pool from the parameters in `quartz.properties`.
- **SchedulerFactoryBean** — configured with DataSource, transactions (PlatformTransactionManager), Spring integration (AutowiringSpringBeanJobFactory), global listeners, and shutdown flags.
- **Spring integration** — `@Autowired` works inside `Job.execute()`.
- **Logging** — startup prints scheduler information such as name, ID, job store, thread pool, clustering, and version.

### Database schema

For persistent mode, create the Quartz tables in your database. The scripts are available in the Quartz distribution:

```
quartz-*.jar/org/quartz/impl/jdbcjobstore/tables_*.sql
```

Choose the `.sql` file that matches your database (for example, `tables_mysql_innodb.sql` for MySQL).

---

## Collision handling strategies

| Type | Behavior |
|-----|----------|
| `FAIL` | Throws `JobCollisionException` |
| `SKIP` | Skips scheduling if the job already exists |
| `REMOVE` | Removes the existing job and creates a new one |
| `SKIP_AND_REPLACE_JOB_DATA` | Skips but updates the JobDataMap |
| `REPLACE_AND_RESCHEDULE_IF_EXISTS` | Reschedules the existing trigger with new parameters (default) |
