package org.activiti.cloud.services.events.message;

import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

public class RuntimeBundleInfoMessageBuilderFilter implements MessageBuilderFilter<CloudRuntimeEvent<?, ?>[]>{
	
    private final RuntimeBundleProperties properties;

    public RuntimeBundleInfoMessageBuilderFilter(RuntimeBundleProperties properties) {
    	Assert.notNull(properties, "properties must not be null");
    	
        this.properties = properties;
    }
	

	@Override
	public MessageBuilder<CloudRuntimeEvent<?, ?>[]> apply(MessageBuilder<CloudRuntimeEvent<?, ?>[]> request) {
		return request.setHeader("appName", properties.getAppName())
				.setHeader("appVersion", properties.getAppVersion())
				.setHeader("serviceName", properties.getServiceName())
				.setHeader("serviceFullName", properties.getServiceFullName())
				.setHeader("serviceType", properties.getServiceType())
				.setHeader("serviceVersion", properties.getServiceVersion());
	}

}
