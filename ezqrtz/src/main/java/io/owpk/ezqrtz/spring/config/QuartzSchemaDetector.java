package io.owpk.ezqrtz.spring.config;

import javax.sql.DataSource;

public interface QuartzSchemaDetector {
    boolean schemaExists(DataSource dataSource);
}
