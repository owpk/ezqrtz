package io.owpk.ezqrtz.management.adapter.internal;

import io.owpk.ezqrtz.management.api.model.AdapterInfo;
import io.owpk.ezqrtz.management.api.DescriableSchedulerManager;
import io.owpk.ezqrtz.management.api.ex.JobNotFound;
import io.owpk.ezqrtz.management.api.model.JobDef;
import io.owpk.ezqrtz.management.api.model.RemoteAdapterInfo;
import io.owpk.ezqrtz.management.api.model.SchedulerStatus;
import io.vavr.control.Try;
import org.jspecify.annotations.NullMarked;
import org.quartz.Scheduler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@NullMarked
public class DefaultDescriableQuartzAdapter extends InternalQuartzSchedulerManagerAdapter implements DescriableSchedulerManager {
    private final AdapterInfo props;

    public DefaultDescriableQuartzAdapter(Scheduler scheduler,
                                          AdapterInfo adapterInfo) {
        super(scheduler);
        this.props = adapterInfo;
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
                .stream().sorted(Comparator.comparing(JobDef::id))
                .toList();
    }

    @Override
    public JobDef<Map<String, Class<?>>> getJobDefinition(String id) {
        return props.jobInfoRegistry().getById(id)
                .orElseThrow(() -> new JobNotFound("Job not found by id: " + id));
    }
}