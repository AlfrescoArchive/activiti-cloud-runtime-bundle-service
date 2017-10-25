package org.activiti.cloud.services.core;

import org.activiti.cloud.services.SecurityPolicy;
import org.activiti.cloud.services.SecurityPolicyService;
import org.activiti.engine.UserGroupLookupProxy;
import org.activiti.engine.UserRoleLookupProxy;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SecurityPolicyApplicationService {

    @Autowired(required = false)
    private UserGroupLookupProxy userGroupLookupProxy;

    @Autowired(required = false)
    private UserRoleLookupProxy userRoleLookupProxy;

    private AuthenticationWrapper authenticationWrapper = new AuthenticationWrapper();

    private final SecurityPolicyService securityPolicyService;

    @Autowired
    public SecurityPolicyApplicationService(SecurityPolicyService securityPolicyService){
        this.securityPolicyService = securityPolicyService;
    }

    public ProcessDefinitionQuery processDefQuery(ProcessDefinitionQuery query, SecurityPolicy securityPolicy){

        if (!securityPolicyService.policiesDefined() || userGroupLookupProxy ==null){
            return query;
        }

        Set<String> keys = securityPolicyService.getProcessDefinitionKeys(authenticationWrapper.getAuthenticatedUserId(),
                userGroupLookupProxy.getGroupsForCandidateUser(authenticationWrapper.getAuthenticatedUserId()), securityPolicy);

        if(keys != null){
            query = query.processDefinitionKeys(keys);
        }
        return query;
    }

    public ProcessInstanceQuery processInstQuery(ProcessInstanceQuery query, SecurityPolicy securityPolicy){
        if (!securityPolicyService.policiesDefined() || userGroupLookupProxy ==null){
            return query;
        }

        Set<String> keys = securityPolicyService.getProcessDefinitionKeys(authenticationWrapper.getAuthenticatedUserId(),
                userGroupLookupProxy.getGroupsForCandidateUser(authenticationWrapper.getAuthenticatedUserId()), securityPolicy);

        if(keys != null){
            query = query.processDefinitionKeys(keys);
        }
        return query;
    }

    public boolean canWrite(String processDefId){
        if(userRoleLookupProxy != null && userRoleLookupProxy.isAdmin(authenticationWrapper.getAuthenticatedUserId())){
            return true;
        }

        if (!securityPolicyService.policiesDefined() || userGroupLookupProxy ==null){
            return true;
        }

        Set<String> keys = securityPolicyService.getProcessDefinitionKeys(authenticationWrapper.getAuthenticatedUserId(),
                userGroupLookupProxy.getGroupsForCandidateUser(authenticationWrapper.getAuthenticatedUserId()), SecurityPolicy.WRITE);

        if (keys != null && keys.contains(processDefId)){
            return true;
        }
        return false;
    }
}
