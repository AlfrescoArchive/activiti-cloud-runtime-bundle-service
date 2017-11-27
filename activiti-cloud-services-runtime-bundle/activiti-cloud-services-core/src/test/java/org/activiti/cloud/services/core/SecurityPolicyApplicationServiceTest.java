package org.activiti.cloud.services.core;

import org.activiti.cloud.services.SecurityPolicy;
import org.activiti.cloud.services.SecurityPolicyService;
import org.activiti.engine.UserGroupLookupProxy;
import org.activiti.engine.UserRoleLookupProxy;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.MockitoAnnotations.initMocks;

public class SecurityPolicyApplicationServiceTest {

    @InjectMocks
    private SecurityPolicyApplicationService securityPolicyApplicationService;

    @Mock
    private UserGroupLookupProxy userGroupLookupProxy;

    @Mock
    private UserRoleLookupProxy userRoleLookupProxy;

    @Mock
    private SecurityPolicyService securityPolicyService;

    @Mock
    private AuthenticationWrapper authenticationWrapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldNotModifyQueryWhenNoPoliciesDefined(){
        ProcessDefinitionQuery query = mock(ProcessDefinitionQuery.class);

        when(securityPolicyService.policiesDefined()).thenReturn(false);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("bob");

        assertThat(securityPolicyApplicationService.processDefQuery(query, SecurityPolicy.READ)).isEqualTo(query);
    }

    @Test
    public void shouldNotModifyQueryWhenNoUser(){
        ProcessDefinitionQuery query = mock(ProcessDefinitionQuery.class);

        when(securityPolicyService.policiesDefined()).thenReturn(true);
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn(null);

        assertThat(securityPolicyApplicationService.processDefQuery(query, SecurityPolicy.READ)).isEqualTo(query);
    }
}
