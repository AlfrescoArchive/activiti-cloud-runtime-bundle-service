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
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
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
        LocalDateTime date = LocalDateTime.now();
        String dateString = formatLocalDateTimeString(date);
        
        //THEN
        Date newDate = dateFormatterProvider.convert2Date(dateString);
        String newDateString = formatLocalDateTimeString(convertDateToLocalDate(newDate));
        
        assertThat(newDateString).isEqualTo(dateString);
    }
    
    @Test
    public void shouldReturnCorrectDateForString() throws Exception {
        //WHEN
        LocalDateTime dt = LocalDateTime.now();
        
        //THEN
        String dateString = formatLocalDateTimeStringWithPattern(dt, "yyyy-MM-dd");
        Date newDate = dateFormatterProvider.convert2Date(dateString);
        String newDateString = formatLocalDateTimeStringWithPattern(convertDateToLocalDate(newDate), "yyyy-MM-dd");

        assertThat(newDateString).isEqualTo(dateString);
    }
    
    @Test
    public void shoulThrowExceptionForWrongFormatString() throws Exception {
        //WHEN
        String dateString = "2020-10-Wrong";
        
        //THEN
        Throwable throwable = catchThrowable(() -> dateFormatterProvider.convert2Date(dateString));

        //THEN
        assertThat(throwable)
                .isInstanceOf(DateTimeException.class);
    }
    
    @Test
    public void shoulThrowExceptionForWrongObject() throws Exception {
      //WHEN
        Throwable throwable = catchThrowable(() -> dateFormatterProvider.convert2Date(10.567));

        //THEN
        assertThat(throwable)
                .isInstanceOf(DateTimeException.class);
    }
 
    private LocalDateTime convertDateToLocalDate(Date date) {
        return date.toInstant()
               .atZone(dateFormatterProvider.getZoneId())
               .toLocalDateTime();
    }
    
    private String formatLocalDateTimeString(LocalDateTime date) {
        return new DateTimeFormatterBuilder()
                   .appendPattern(dateFormatterProvider.getDateFormatPattern())
                   .toFormatter()
                   .withZone(dateFormatterProvider.getZoneId())
                   .format(date);
    }
    
    public String formatLocalDateTimeStringWithPattern(LocalDateTime date, String datePattern) {
        return new DateTimeFormatterBuilder()
                  .appendPattern(datePattern)
                  .toFormatter()
                  .withZone(dateFormatterProvider.getZoneId())
                  .format(date);
    }   
}