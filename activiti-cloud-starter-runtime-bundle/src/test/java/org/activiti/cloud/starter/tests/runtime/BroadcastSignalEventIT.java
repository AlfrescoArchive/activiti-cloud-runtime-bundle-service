package org.activiti.cloud.starter.tests.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.services.subscription.impl.cmd.BroadcastSignalsCmd;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BroadcastSignalEventIT {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managemenService;

    @Test
    public void shouldBroadcastSignals() throws Exception {
        //when
        ProcessInstance procInst1 = runtimeService.startProcessInstanceByKey("broadcastSignalCatchEventProcess");
        ProcessInstance procInst2 = runtimeService.startProcessInstanceByKey("broadcastSignalEventProcess");
        assertThat(procInst1).isNotNull();
        assertThat(procInst2).isNotNull();

        await("Broadcast Signals").untilAsserted(() -> {
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceId(procInst1.getId()).list();
            assertThat(processInstances).isEmpty();
        });

        //then
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceId(procInst1.getId()).list();
        assertThat(processInstances).isEmpty();
    }

    @Test
    public void shouldBroadcastSignalsByListener() throws Exception {
        //when
        ProcessInstance procInst1 = runtimeService.startProcessInstanceByKey("broadcastSignalCatchEventProcess");
        ProcessInstance procInst2 = runtimeService.startProcessInstanceByKey("broadcastSignalEventProcessByListener");
        assertThat(procInst1).isNotNull();
        assertThat(procInst2).isNotNull();

        await("Broadcast Signals").untilAsserted(() -> {
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceId(procInst1.getId()).list();
            assertThat(processInstances).isEmpty();
        });

        //then
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceId(procInst1.getId()).list();
        assertThat(processInstances).isEmpty();
    }

    @Test
    public void shouldBroadcastSignalsByCmd() throws Exception {
        //when
        ProcessInstance procInst1 = runtimeService.startProcessInstanceByKey("broadcastSignalCatchEventProcess");
        assertThat(procInst1).isNotNull();
        managemenService.executeCommand(new BroadcastSignalsCmd("Test", false));

        await("Broadcast Signals").untilAsserted(() -> {
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceId(procInst1.getId()).list();
            assertThat(processInstances).isEmpty();
        });

        //then
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceId(procInst1.getId()).list();
        assertThat(processInstances).isEmpty();
    }
}
