package org.activiti.cloud.services.events.message;

public interface MessageBuilderFilterChainFactory<P, T> {
	public MessageBuilderFilterChain<P> create(T context);
}
