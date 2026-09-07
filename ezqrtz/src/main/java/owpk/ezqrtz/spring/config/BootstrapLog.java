package owpk.ezqrtz.spring.config;

import lombok.Builder;

@Builder
public record BootstrapLog(
        String schedulerName,
        String instanceId,
        String schedulerClass,
        boolean started,
        boolean inStandbyMode,
        boolean shutdown,
        String jobStoreClass,
        String threadPoolClass,
        long numberOfJobsExecuted,
        boolean clustered,
        String version
) {
}
