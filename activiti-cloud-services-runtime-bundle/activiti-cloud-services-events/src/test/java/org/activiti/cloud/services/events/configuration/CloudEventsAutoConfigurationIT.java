package org.activiti.cloud.services.events.configuration;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.NONE)
public class CloudEventsAutoConfigurationIT {
	
	@SpringBootApplication
	static class MockRuntimeBundleApplication {
		@MockBean
		private ProcessEngineChannels processEngineChannels;

		@MockBean
		private RuntimeService runtimeService;

		@MockBean
		private TaskService taskService;
		
		@MockBean
		private UserGroupManager userGroupManager;
		
		@MockBean
		private SecurityManager securityManager;
		
		@MockBean
		private RepositoryService repositoryService;
		
		@MockBean
		private ProcessSecurityPoliciesManager processSecurityPoliciesManager;
	}
	
	@Test
	public void contextLoads() {
		// success
	}

}
