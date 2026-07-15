package owpk.ezqrtz.spring.config;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DefaultQuartzSchemaDetector implements QuartzSchemaDetector {

    private static final String[] TABLE_NAMES = {
            "QRTZ_JOB_DETAILS", "qrtz_job_details", "QRTZ_LOCKS", "qrtz_locks"
    };

    @Override
    public boolean quartzSchemaExists(DataSource dataSource) {
        try (var conn = dataSource.getConnection()) {
            var meta = conn.getMetaData();
            for (String table : TABLE_NAMES) {
                try (var rs = meta.getTables(null, null, table, new String[]{"TABLE"})) {
                    if (rs.next())
                        return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check Quartz schema", e);
        }
    }
}
