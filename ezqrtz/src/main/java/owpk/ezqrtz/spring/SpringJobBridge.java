package owpk.ezqrtz.spring;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringJobBridge implements Job {

    @SuppressWarnings("all")
    @Autowired
    private CztQuartzJobRegistrar registrar;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var beanId = context.getMergedJobDataMap()
                .getString("__handler");
        var regOpt = registrar.get(beanId);
        if (regOpt.isPresent()) {
            try {
                regOpt.get().methodHandle().invoke(context);
            } catch (Throwable e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
