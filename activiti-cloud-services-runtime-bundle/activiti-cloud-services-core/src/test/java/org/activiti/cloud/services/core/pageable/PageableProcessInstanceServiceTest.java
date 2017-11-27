package org.activiti.cloud.services.core.pageable;

import org.activiti.cloud.services.SecurityPolicy;
import org.activiti.cloud.services.api.model.converter.ProcessInstanceConverter;
import org.activiti.cloud.services.core.SecurityPolicyApplicationService;
import org.activiti.cloud.services.core.pageable.sort.ProcessInstanceSortApplier;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PageableProcessInstanceServiceTest {

    @InjectMocks
    private PageableProcessInstanceService pageableProcessInstanceService;

    @Mock
    private PageRetriever pageRetriever;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private ProcessInstanceSortApplier sortApplier;

    @Mock
    private ProcessInstanceConverter processInstanceConverter;

    @Mock
    private SecurityPolicyApplicationService securityService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldApplySecurity(){
        Pageable pageable = mock(Pageable.class);
        ProcessInstanceQuery query = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
        when(securityService.processInstQuery(query, SecurityPolicy.READ)).thenReturn(query);

        pageableProcessInstanceService.getProcessInstances(pageable);

        verify(securityService).processInstQuery(query,SecurityPolicy.READ);
    }
}
