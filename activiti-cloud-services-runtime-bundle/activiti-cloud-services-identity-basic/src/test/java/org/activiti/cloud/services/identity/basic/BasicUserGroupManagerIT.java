//package org.activiti.cloud.services.identity.basic;
//
//import org.activiti.runtime.api.identity.UserGroupManager;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//public class BasicUserGroupManagerIT {
//
//    @Autowired
//    private UserGroupManager userGroupManager;
//
//    @Test
//    public void testAdminRole() throws Exception {
//        assertThat(userGroupManager.getUserRoles("client")).contains("admin");
//        assertThat(userGroupManager.getUserRoles("testuser")).doesNotContain("admin");
//    }
//
//    @org.springframework.context.annotation.Configuration
//    @ComponentScan("org.activiti.spring.identity")
//    public static class Configuration {
//
//    }
//}
