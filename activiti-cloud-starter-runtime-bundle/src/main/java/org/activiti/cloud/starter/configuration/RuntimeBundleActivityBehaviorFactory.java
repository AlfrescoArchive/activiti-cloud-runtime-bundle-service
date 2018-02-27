package org.activiti.cloud.starter.configuration;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.services.connectors.behavior.MQServiceTaskBehavior;
import org.springframework.beans.factory.annotation.Autowired;

public class RuntimeBundleActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    @Autowired
    private MQServiceTaskBehavior behavior;
    
    @Override
    public ActivityBehavior createDefaultServiceTaskBehavior(ServiceTask serviceTask) {
        return behavior;
    }
}
