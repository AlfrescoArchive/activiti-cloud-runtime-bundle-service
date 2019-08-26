/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.rest.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Date;

import org.activiti.api.runtime.conf.impl.CommonModelAutoConfiguration;
import org.activiti.api.runtime.conf.impl.ProcessModelAutoConfiguration;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.CloudEventsAutoConfiguration;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.rest.conf.ServicesRestAutoConfiguration;
import org.activiti.spring.process.conf.ProcessExtensionsAutoConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(DateFormatterProvider.class)
@EnableSpringDataWebSupport
@AutoConfigureMockMvc(secure = false)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Import({CommonModelAutoConfiguration.class,
         ProcessModelAutoConfiguration.class,
         RuntimeBundleProperties.class,
         CloudEventsAutoConfiguration.class,
         ProcessExtensionsAutoConfiguration.class,
         ServicesRestAutoConfiguration.class})
@ComponentScan(basePackages = {"org.activiti.cloud.services.rest.assemblers", "org.activiti.cloud.alfresco"})
public class DateFormatterProviderIT {
 
    @MockBean
    private ProcessEngineChannels processEngineChannels;
    
    @Autowired
    private DateFormatterProvider dateFormatterProvider;
    
    @Before
    public void setUp() {
 
    }
    
    @Test
    public void shouldReturnCorrectFormatPattern() throws Exception {
        assertThat(dateFormatterProvider.getDateFormatPattern()).isEqualTo("yyyy-MM-dd[['T'][ ]HH:mm:ss[.SSS'Z']]");
    }
 
    @Test
    public void shouldReturnCorrectDateTimeFromDateTime() throws Exception {
        //WHEN
        Date dateTime = new Date();
        
        //THEN
        assertThat(dateFormatterProvider.convert2Date(dateTime)).isEqualTo(dateTime);
    }
    
    @Test
    public void shouldReturnCorrectDateTimeForString() throws Exception {
        //WHEN
        LocalDateTime dt = LocalDateTime.now();
        String dateStr = dateFormatterProvider.formatLocalDateTimeString(dt);
        
        //THEN
        Date newDate = dateFormatterProvider.convert2Date(dateStr);
        String newDateStr = dateFormatterProvider
                            .formatLocalDateTimeString(dateFormatterProvider.convertDateToLocalDate(newDate));
        
        assertThat(newDateStr).isEqualTo(dateStr);
    }
    
    @Test
    public void shouldReturnCorrectDateForString() throws Exception {
        //WHEN
        LocalDateTime dt = LocalDateTime.now();
        
        //THEN
        String localStr = dateFormatterProvider.formatLocalDateTimeStringWithPattern(dt, "yyyy-MM-dd");
        Date newDate = dateFormatterProvider.convert2Date(localStr);
        LocalDateTime newLocalDate = dateFormatterProvider.convertDateToLocalDate(newDate);
        String newLocalStr = dateFormatterProvider.formatLocalDateTimeStringWithPattern(newLocalDate, "yyyy-MM-dd");

        assertThat(newLocalStr).isEqualTo(localStr);
    }
 
}