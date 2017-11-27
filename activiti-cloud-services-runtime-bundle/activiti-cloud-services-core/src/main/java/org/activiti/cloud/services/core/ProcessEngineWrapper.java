package org.activiti.cloud.services.core;

import java.util.List;
import java.util.Map;

import org.activiti.cloud.services.SecurityPolicy;
import org.activiti.cloud.services.api.commands.ActivateProcessInstanceCmd;
import org.activiti.cloud.services.api.commands.ClaimTaskCmd;
import org.activiti.cloud.services.api.commands.CompleteTaskCmd;
import org.activiti.cloud.services.api.commands.ReleaseTaskCmd;
import org.activiti.cloud.services.api.commands.SetTaskVariablesCmd;
import org.activiti.cloud.services.api.commands.SignalProcessInstancesCmd;
import org.activiti.cloud.services.api.commands.StartProcessInstanceCmd;
import org.activiti.cloud.services.api.commands.SuspendProcessInstanceCmd;
import org.activiti.cloud.services.api.model.ProcessInstance;
import org.activiti.cloud.services.api.model.Task;
import org.activiti.cloud.services.api.model.converter.ProcessInstanceConverter;
import org.activiti.cloud.services.api.model.converter.TaskConverter;
import org.activiti.cloud.services.core.pageable.PageableProcessInstanceService;
import org.activiti.cloud.services.core.pageable.PageableTaskService;
import org.activiti.cloud.services.events.MessageProducerActivitiEventListener;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ProcessEngineWrapper {

    private final ProcessInstanceConverter processInstanceConverter;
    private final RuntimeService runtimeService;
    private PageableProcessInstanceService pageableProcessInstanceService;
    private final TaskService taskService;
    private final TaskConverter taskConverter;
    private final PageableTaskService pageableTaskService;
    private final SecurityPolicyApplicationService securityService;
    private final RepositoryService repositoryService;

    @Autowired
    public ProcessEngineWrapper(ProcessInstanceConverter processInstanceConverter,
                                RuntimeService runtimeService,
                                PageableProcessInstanceService pageableProcessInstanceService,
                                TaskService taskService,
                                TaskConverter taskConverter,
                                PageableTaskService pageableTaskService,
                                MessageProducerActivitiEventListener listener,
                                SecurityPolicyApplicationService securityService,
                                RepositoryService repositoryService) {
        this.processInstanceConverter = processInstanceConverter;
        this.runtimeService = runtimeService;
        this.pageableProcessInstanceService = pageableProcessInstanceService;
        this.taskService = taskService;
        this.taskConverter = taskConverter;
        this.pageableTaskService = pageableTaskService;
        this.runtimeService.addEventListener(listener);
        this.securityService = securityService;
        this.repositoryService = repositoryService;
    }

    public Page<ProcessInstance> getProcessInstances(Pageable pageable) {
        return pageableProcessInstanceService.getProcessInstances(pageable);
    }

    public ProcessInstance startProcess(StartProcessInstanceCmd cmd) {

        if (!securityService.canWrite(getProcessDefinitionKeyById(cmd.getProcessDefinitionId()))){
            throw new ActivitiForbiddenException("Operation not permitted");
        }

        ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder();
        builder.processDefinitionId(cmd.getProcessDefinitionId());
        builder.variables(cmd.getVariables());
        return processInstanceConverter.from(builder.start());
    }

    public void signal(SignalProcessInstancesCmd signalProcessInstancesCmd) {
        //TODO: ideally we'd restrict signalling to just the process defs that the user has access to
        // but that would require overloading RuntimeService.signalEventReceived within the engine (see SignalEventReceivedCmd)
        // so that we could pass a query restriction parameter down to the engine
        // for now we instead just check that user has write access to at least one of the process definitions in the RB

        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        query = securityService.processDefQuery(query, SecurityPolicy.WRITE);

        if(query.count()>0) {
            runtimeService.signalEventReceived(signalProcessInstancesCmd.getName(),
                    signalProcessInstancesCmd.getInputVariables());
        }

    }

    public void suspend(SuspendProcessInstanceCmd suspendProcessInstanceCmd) {
        ProcessInstance processInstance = getProcessInstanceById(suspendProcessInstanceCmd.getProcessInstanceId());

        verifyCanModifyProcessInstance(processInstance, "Unable to find process instance for the given id:'" + suspendProcessInstanceCmd.getProcessInstanceId() + "'", getProcessDefinitionKeyById(processInstance.getProcessDefinitionId()));
        runtimeService.suspendProcessInstanceById(suspendProcessInstanceCmd.getProcessInstanceId());
    }

    public String getProcessDefinitionKeyById(String id){
        return repositoryService.getProcessDefinition(id).getKey();
    }

    private void verifyCanModifyProcessInstance(ProcessInstance processInstance, String message, String processDefinitionId) {
        if (processInstance == null) {
            throw new ActivitiException(message);
        }
        if (!securityService.canWrite(processDefinitionId)) {
            throw new ActivitiForbiddenException("Operation not permitted");
        }
    }

    public void activate(ActivateProcessInstanceCmd activateProcessInstanceCmd) {
        ProcessInstance processInstance = getProcessInstanceById(activateProcessInstanceCmd.getProcessInstanceId());

        verifyCanModifyProcessInstance(processInstance, "Unable to find process instance for the given id:'" + activateProcessInstanceCmd.getProcessInstanceId() + "'", getProcessDefinitionKeyById(processInstance.getProcessDefinitionId()));
        runtimeService.activateProcessInstanceById(activateProcessInstanceCmd.getProcessInstanceId());
    }

    public ProcessInstance getProcessInstanceById(String processInstanceId) {
        org.activiti.engine.runtime.ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        return processInstanceConverter.from(processInstance);
    }

    public List<String> getActiveActivityIds(String executionId) {
        return runtimeService.getActiveActivityIds(executionId);
    }

    public Page<Task> getTasks(Pageable pageable) {
        return pageableTaskService.getTasks(pageable);
    }

    public Task getTaskById(String taskId) {
        org.activiti.engine.task.Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return taskConverter.from(task);
    }

    public Task claimTask(ClaimTaskCmd claimTaskCmd) {
        taskService.claim(claimTaskCmd.getTaskId(),
                          claimTaskCmd.getAssignee());
        return taskConverter.from(taskService.createTaskQuery().taskId(claimTaskCmd.getTaskId()).singleResult());
    }

    public Task releaseTask(ReleaseTaskCmd releaseTaskCmd) {
        taskService.unclaim(releaseTaskCmd.getTaskId());
        return taskConverter.from(taskService.createTaskQuery().taskId(releaseTaskCmd.getTaskId()).singleResult());
    }

    public void completeTask(CompleteTaskCmd completeTaskCmd) {
        Map<String, Object> outputVariables = null;
        if (completeTaskCmd != null) {
            outputVariables = completeTaskCmd.getOutputVariables();
        }
        taskService.complete(completeTaskCmd.getTaskId(),
                             outputVariables);
    }

    public void setTaskVariables(SetTaskVariablesCmd setTaskVariablesCmd) {
        taskService.setVariables(setTaskVariablesCmd.getTaskId(),
                                 setTaskVariablesCmd.getVariables());
    }

    public void setTaskVariablesLocal(SetTaskVariablesCmd setTaskVariablesCmd) {
        taskService.setVariablesLocal(setTaskVariablesCmd.getTaskId(),
                                      setTaskVariablesCmd.getVariables());
    }
}
