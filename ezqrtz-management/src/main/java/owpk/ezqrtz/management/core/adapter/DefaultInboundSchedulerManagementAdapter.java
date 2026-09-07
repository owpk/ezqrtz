package owpk.ezqrtz.management.core.adapter;

import owpk.ezqrtz.management.api.InternalAdapterProps;
import owpk.ezqrtz.management.api.InboundSchedulerManager;
import owpk.ezqrtz.management.api.rest.ex.JobNotFound;
import owpk.ezqrtz.management.api.model.JobDef;
import owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import owpk.ezqrtz.management.api.model.SchedulerStatus;
import io.vavr.control.Try;
import org.quartz.Scheduler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DefaultInboundSchedulerManagementAdapter extends QuartzSchedulerManagerAdapter implements InboundSchedulerManager {
    private final InternalAdapterProps props;

    public DefaultInboundSchedulerManagementAdapter(Scheduler scheduler,
                                                    InternalAdapterProps internalAdapterProps) {
        super(scheduler);
        this.props = internalAdapterProps;
    }

    @Override
    public RemoteAdapterInfo getInfo() {
        return Try.of(scheduler::getMetaData)
                .mapTry(it -> buildInfo(it.getSummary(), SchedulerStatus.STARTED, it.getSchedulerName(),
                        props.adapterId()))
                .getOrElseGet(e -> buildInfo(props.adapterDesc(), SchedulerStatus.ERROR, "Unknown scheduler instance",
                        props.adapterId()));
    }

    @Override
    public List<JobDef<Map<String, Class<?>>>> listJobs() {
        return props.jobInfoRegistry().getAllJobs()
                .stream().sorted(Comparator.comparing(JobDef::getId))
                .toList();
    }

    @Override
    public JobDef<Map<String, Class<?>>> getJobDefinition(String id) {
        return props.jobInfoRegistry().getById(id)
                .orElseThrow(() -> new JobNotFound("Job not found by id: " + id));
    }
}