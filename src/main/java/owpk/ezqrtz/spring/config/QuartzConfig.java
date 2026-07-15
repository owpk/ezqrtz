package owpk.ezqrtz.spring.config;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import owpk.ezqrtz.api.SchedulerInterceptor;
import owpk.ezqrtz.service.JobsListenerService;
import owpk.ezqrtz.spring.EzQuartzJobRegistrar;
import owpk.ezqrtz.spring.QuartzBeanPostProcessor;
import owpk.ezqrtz.spring.QuartzStartup;

@RequiredArgsConstructor
@Slf4j
public class QuartzConfig {

    //@formatter:off
    public static final String PROPS_DS_DRIVER =    "org.quartz.dataSource.quartzDataSource.driver";
    public static final String PROPS_DS_URLS =      "org.quartz.dataSource.quartzDataSource.URL";
    public static final String PROPS_DS_USER =      "org.quartz.dataSource.quartzDataSource.user";
    public static final String PROPS_DS_PASS =      "org.quartz.dataSource.quartzDataSource.password";
    public static final String PROPS_DS_MAX_CONN =  "org.quartz.dataSource.quartzDataSource.maxConnections";
    //@formatter:on

    private final Environment environment;

    @Value("${ez.quartz.config.path:quartz.properties}")
    private String quartzConfigPath;

    @Bean
    EzQuartzJobRegistrar jobRegistrar(Scheduler sfb, List<SchedulerInterceptor> interceptors) {
        return new EzQuartzJobRegistrar(sfb, interceptors);
    }

    @Bean
    QuartzBeanPostProcessor quartzBeanPostProcessor(EzQuartzJobRegistrar registrar) {
        return new QuartzBeanPostProcessor(registrar);
    }

    @Bean
    QuartzStartup quartzStartup(EzQuartzJobRegistrar registrar) {
        return new QuartzStartup(registrar);
    }

    @Bean
    @ConditionalOnMissingBean
    QuartzSchemaDetector defaultQuartzSchemaDetector() {
        return new DefaultQuartzSchemaDetector();
    }

    @Bean
    QuartzSchemaInitializer quartzSchemaInitializer(@Qualifier("quartzDataSource") DataSource quartzDataSource,
                                                    QuartzSchemaDetector quartzSchemaDetector) {
        return new QuartzSchemaInitializer(quartzDataSource, environment, quartzSchemaDetector);
    }

    /**
     * C3P0 default pool
     * <a href=
     * "https://www.mchange.com/projects/c3p0/#using_combopooleddatasource">c3po
     * pool config</a>
     */
    @Bean(name = "quartzDataSource")
    DataSource quartzDataSource() throws IOException {
        var props = quartzProps();
        var dsDriver = prop(props, PROPS_DS_DRIVER);
        var dsUrl = prop(props, PROPS_DS_URLS);
        var dsUser = prop(props, PROPS_DS_USER);
        var dsPass = prop(props, PROPS_DS_PASS);
        var maxConn = Integer.parseInt(prop(props, PROPS_DS_MAX_CONN));

        var cpds = new ComboPooledDataSource();
        cpds.setDataSourceName("quartzDataSource");
        try {
            cpds.setDriverClass(dsDriver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        cpds.setJdbcUrl(dsUrl);
        cpds.setUser(dsUser);
        cpds.setPassword(dsPass);
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(maxConn);
        cpds.setIdleConnectionTestPeriod(60);
        cpds.setPreferredTestQuery("SELECT 1");
        cpds.setMaxIdleTimeExcessConnections(120);

        return cpds;
    }

    private String prop(Properties props, String name) {
        return (String) props.get(name);
    }

    /**
     * Main quartz bean configuration
     */
    @Bean
    SchedulerFactoryBean schedulerFactoryBean(
            PlatformTransactionManager txManager,
            SpringBeanJobFactory jobFactory,
            JobsListenerService jobsListenerService,
            @Qualifier("quartzDataSource") DataSource quartzDataSource,
            Trigger... triggers) throws Exception {

        var props = quartzProps();
        var schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setQuartzProperties(props);
        schedulerFactory.setDataSource(quartzDataSource);
        schedulerFactory.setTransactionManager(txManager);
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setGlobalJobListeners(jobsListenerService);
        schedulerFactory.setStartupDelay(5);
        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);

        if (triggers != null && triggers.length > 0) {
            schedulerFactory.setTriggers(triggers);
            log.info("Quartz triggers: {}", triggers.length);
        }

        return schedulerFactory;
    }

    /**
     * Job factory with current application context to inject beans into job
     * instances
     */
    @Bean
    SpringBeanJobFactory springBeanJobFactory(ApplicationContext ctx) {
        var jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(ctx);
        return jobFactory;
    }

    /**
     * Quartz properties
     */
    private Properties quartzProps() throws IOException {
        var factoryBean = new PropertiesFactoryBean();
        factoryBean.setLocation(new ClassPathResource(quartzConfigPath));
        factoryBean.afterPropertiesSet();

        var props = factoryBean.getObject();
        var propsWithVariables = new Properties();

        Objects.requireNonNull(props)
                .stringPropertyNames()
                .forEach(name -> propsWithVariables.put(
                    name, environment.resolvePlaceholders(props.getProperty(name))));

        return propsWithVariables;
    }

    @Bean
    CommandLineRunner printQuartzConfig(SchedulerFactoryBean schedulerFactoryBean) {
        return args -> {
            var scheduler = schedulerFactoryBean.getScheduler();
            log.info("""

                    --- Ez Quartz Scheduler Info ---
                    Scheduler Name: {}
                    Instance ID: {}
                    Scheduler Class: {}
                    Is Started: {}
                    Is In Standby Mode: {}
                    Is Shutdown: {}
                    Job Store Class: {}
                    Thread Pool Class: {}
                    Number of Jobs Executed: {}
                    Clustered: {}
                    Version: {}
                    --------------------------------

                    """, scheduler.getSchedulerName(),
                    scheduler.getSchedulerInstanceId(),
                    scheduler.getClass().getName(),
                    scheduler.isStarted(),
                    scheduler.isInStandbyMode(),
                    scheduler.isShutdown(),
                    scheduler.getMetaData().getJobStoreClass().getName(),
                    scheduler.getMetaData().getThreadPoolClass().getName(),
                    scheduler.getMetaData().getNumberOfJobsExecuted(),
                    scheduler.getMetaData().isJobStoreClustered(),
                    scheduler.getMetaData().getVersion());
        };
    }
}