package io.opwk.ezqrtz.config;

import javax.sql.DataSource;

public interface QuartzSchemaDetector {
    boolean schemaExists(DataSource dataSource);
}
