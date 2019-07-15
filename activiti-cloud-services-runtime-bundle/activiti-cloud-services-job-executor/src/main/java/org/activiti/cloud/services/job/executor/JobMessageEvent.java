/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.cloud.services.job.executor;

import java.util.Objects;

import org.springframework.context.ApplicationEvent;

public class JobMessageEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final String destination;
    private final String jobId;
    
    public JobMessageEvent(String jobId, String destination, Object source) {
        super(source);
        this.jobId = jobId;
        this.destination = destination;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public String getJobId() {
        return jobId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, jobId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobMessageEvent other = (JobMessageEvent) obj;
        return Objects.equals(destination, other.destination) && Objects.equals(jobId, other.jobId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JobMessageEvent [destination=");
        builder.append(destination);
        builder.append(", jobId=");
        builder.append(jobId);
        builder.append(", source=");
        builder.append(source);
        builder.append("]");
        return builder.toString();
    }


}
