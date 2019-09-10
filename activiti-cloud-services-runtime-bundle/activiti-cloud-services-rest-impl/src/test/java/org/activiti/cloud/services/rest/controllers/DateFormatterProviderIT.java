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

import org.activiti.spring.process.variable.DateFormatterProvider;
import org.junit.Before;
import org.junit.Test;


public class DateFormatterProviderIT {
    private String dateFormatPattern = "yyyy-MM-dd[['T'][ ]HH:mm:ss[.SSS'Z']]";

    private DateFormatterProvider dateFormatterProvider = new DateFormatterProvider(dateFormatPattern);
    
    @Before
    public void setUp() {
 
    }
    
    @Test
    public void shouldReturnCorrectFormatPattern() throws Exception {
        assertThat(dateFormatterProvider.getDateFormatPattern()).isEqualTo(dateFormatPattern);
    }
 
    @Test
    public void shouldReturnCorrectDateTimeFromDateTime() throws Exception {
        //WHEN
        Date dateTime = new Date();
        
        //THEN
        assertThat(dateFormatterProvider.toDate(dateTime)).isEqualTo(dateTime);
    }
    
    @Test
    public void shouldReturnCorrectDateTimeForString() throws Exception {
        //WHEN
        LocalDateTime date = LocalDateTime.now();
        String dateString = formatLocalDateTimeString(date);
        
        //THEN
        Date newDate = dateFormatterProvider.toDate(dateString);
        String newDateString = formatLocalDateTimeString(convertDateToLocalDate(newDate));
        
        assertThat(newDateString).isEqualTo(dateString);
    }
    
    @Test
    public void shouldReturnCorrectDateForString() throws Exception {
        //WHEN
        LocalDateTime dt = LocalDateTime.now();
        
        //THEN
        String dateString = formatLocalDateTimeStringWithPattern(dt, "yyyy-MM-dd");
        Date newDate = dateFormatterProvider.toDate(dateString);
        String newDateString = formatLocalDateTimeStringWithPattern(convertDateToLocalDate(newDate), "yyyy-MM-dd");

        assertThat(newDateString).isEqualTo(dateString);
    }
    
    @Test
    public void shoulThrowExceptionForWrongFormatString() throws Exception {
        //WHEN
        String dateString = "2020-10-Wrong";
        
        //THEN
        Throwable throwable = catchThrowable(() -> dateFormatterProvider.toDate(dateString));

        //THEN
        assertThat(throwable)
                .isInstanceOf(DateTimeException.class);
    }
    
    @Test
    public void shoulThrowExceptionForWrongObject() throws Exception {
      //WHEN
        Throwable throwable = catchThrowable(() -> dateFormatterProvider.toDate(10.567));

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