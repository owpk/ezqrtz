package owpk.ezqrtz.spring.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QuartzSchemaInitializer implements InitializingBean {

    //@formatter:off
    public static final String ENABLED_PROPERTY =   "ez.quartz.schema.initialize";
    public static final String SCRIPT_PROPERTY =    "ez.quartz.schema.script";
    public static final String JOB_STORE_CLASS_PROPERTY = "org.quartz.jobStore.class";
    //@formatter:on

    private final QuartzSchemaDetector schemaDetector;
    private final DataSource dataSource;
    private final Environment environment;

    @Override
    public void afterPropertiesSet() {
        if (!isEnabled()) {
            log.info("Quartz schema initialization is disabled");
            return;
        }

        if (!shouldInitialize()) {
            log.info("Quartz schema initialization skipped because JDBC job store is not configured");
            return;
        }

        if (schemaExists()) {
            log.info("Quartz schema initialization skipped because it already exists");
            return;
        }

        initialize();
    }

    public void initialize() {
        String location = resolveScriptLocation();

        try {
            var resource = new ClassPathResource(location);
            if (!resource.exists()) {
                log.warn("Quartz schema script not found at classpath location: {}", location);
                return;
            }

            var populator = new ResourceDatabasePopulator(resource);
            populator.setContinueOnError(false);
            populator.setIgnoreFailedDrops(false);
            DatabasePopulatorUtils.execute(populator, dataSource);

            log.info("Quartz schema initialized from {}", location);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize Quartz schema from " + location, ex);
        }
    }

    private boolean isEnabled() {
        return environment.getProperty(ENABLED_PROPERTY, Boolean.class, true);
    }

    private boolean shouldInitialize() {
        String jobStoreClass = environment.getProperty(JOB_STORE_CLASS_PROPERTY);
        return !StringUtils.hasText(jobStoreClass) || jobStoreClass.contains("jdbcjobstore");
    }

    private boolean schemaExists() {
        return schemaDetector.schemaExists(dataSource);
    }

    private String resolveScriptLocation() {
        String configured = environment.getProperty(SCRIPT_PROPERTY, "quartz-init.sql");
        if (configured.startsWith("classpath:")) {
            return configured.substring("classpath:".length());
        }
        return configured;
    }
}
