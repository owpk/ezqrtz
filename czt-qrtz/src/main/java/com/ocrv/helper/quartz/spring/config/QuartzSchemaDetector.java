package com.ocrv.helper.quartz.spring.config;

import javax.sql.DataSource;

public interface QuartzSchemaDetector {
    boolean schemaExists(DataSource dataSource);
}
