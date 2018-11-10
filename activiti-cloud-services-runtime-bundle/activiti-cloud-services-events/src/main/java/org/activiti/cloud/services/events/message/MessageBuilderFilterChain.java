package org.activiti.cloud.services.events.message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class MessageBuilderFilterChain<P> {

	private final List<MessageBuilderFilter<P>> filters = new ArrayList<>();

	public Message<P> build(MessageBuilder<P> request) {
    	for (MessageBuilderFilter<P> filter : filters) {
            filter.apply(request);
         }
    	
         return request.build();
    }
    
    public MessageBuilderFilterChain<P> withFilter(MessageBuilderFilter<P> filter) {
        filters.add(filter);
        
        return this;
    }    

}
