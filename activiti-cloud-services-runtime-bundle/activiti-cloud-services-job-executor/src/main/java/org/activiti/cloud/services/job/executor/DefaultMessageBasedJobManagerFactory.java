package org.activiti.cloud.services.job.executor;

import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;

public class DefaultMessageBasedJobManagerFactory implements MessageBasedJobManagerFactory {
    
    private final RuntimeBundleProperties runtimeBundleProperties;
    private final BinderAwareChannelResolver resolver;
    
    public DefaultMessageBasedJobManagerFactory(RuntimeBundleProperties runtimeBundleProperties,
                                                BinderAwareChannelResolver resolver) {
        this.runtimeBundleProperties = runtimeBundleProperties;
        this.resolver = resolver;
    }

    @Override
    public MessageBasedJobManager create(ProcessEngineConfigurationImpl processEngineConfiguration) {
        return new MessageBasedJobManager(processEngineConfiguration,
                                          runtimeBundleProperties,
                                          resolver);
    }

}
