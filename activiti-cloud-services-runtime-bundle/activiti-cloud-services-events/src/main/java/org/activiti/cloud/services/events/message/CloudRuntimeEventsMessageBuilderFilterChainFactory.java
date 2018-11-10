package org.activiti.cloud.services.events.message;

import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.springframework.util.Assert;

public class CloudRuntimeEventsMessageBuilderFilterChainFactory implements MessageBuilderFilterChainFactory<CloudRuntimeEvent<?, ?>[], CommandContext>{

	private final RuntimeBundleProperties properties;
	
    public CloudRuntimeEventsMessageBuilderFilterChainFactory(RuntimeBundleProperties properties) {
    	Assert.notNull(properties, "properties must not be null");
    	
		this.properties = properties;
	}
	
	@Override
	public MessageBuilderFilterChain<CloudRuntimeEvent<?, ?>[]> create(CommandContext commandContext) {
		return new CloudRuntimeEventsMessageBuilderFilterChain()
				.withFilter(new RuntimeBundleInfoMessageBuilderFilter(properties))				
				.withFilter(new CommandContextMessageBuilderFilter(commandContext));
	}

}
