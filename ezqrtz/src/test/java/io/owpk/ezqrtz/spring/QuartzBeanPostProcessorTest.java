package io.owpk.ezqrtz.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.owpk.ezqrtz.core.annotations.Execute;
import io.owpk.ezqrtz.core.annotations.EzCronJob;

class QuartzBeanPostProcessorTest {

    @Test
    void doesNotResolveRegistrarForUnannotatedBean() {
        var provider = new CountingRegistrarProvider();
        var postProcessor = new QuartzBeanPostProcessor(provider);

        postProcessor.postProcessAfterInitialization(new Object(), "bean");

        assertEquals(0, provider.resolutionCount);
    }

    @Test
    void resolvesRegistrarWhenProcessingQuartzJob() {
        var registrar = new EzQuartzJobRegistrar(null, java.util.List.of());
        var postProcessor = new QuartzBeanPostProcessor(new FixedRegistrarProvider(registrar));

        postProcessor.postProcessAfterInitialization(new QuartzJob(), "job");

        assertEquals(1, registrar.registrations().size());
    }

    @EzCronJob(cron = "0/5 * * * * ?", description = "test", name = "test", group = "test")
    private static final class QuartzJob {
        @Execute
        private void execute() {
            throw new UnsupportedOperationException("Test fixture method should not be invoked");
        }
    }

    private static final class CountingRegistrarProvider implements ObjectProvider<EzQuartzJobRegistrar> {
        private int resolutionCount;

        @Override
        public EzQuartzJobRegistrar getObject() {
            resolutionCount++;
            throw new AssertionError("Registrar should not be resolved");
        }
    }

    private record FixedRegistrarProvider(EzQuartzJobRegistrar registrar)
            implements ObjectProvider<EzQuartzJobRegistrar> {
        @Override
        public EzQuartzJobRegistrar getObject() {
            return registrar;
        }
    }
}