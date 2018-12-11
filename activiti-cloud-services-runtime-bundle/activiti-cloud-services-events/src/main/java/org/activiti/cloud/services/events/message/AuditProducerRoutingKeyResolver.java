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

package org.activiti.cloud.services.events.message;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuditProducerRoutingKeyResolver implements RoutingKeyResolver<Map<String, Object>> {
    
    private static final String UNDERSCORE = "_";
    
    @Override
    public String resolve(Map<String, Object> headers) {
        return Stream.of(RuntimeBundleInfoMessageHeaders.SERVICE_NAME,
                         RuntimeBundleInfoMessageHeaders.APP_NAME,
                         ExecutionContextMessageHeaders.PROCESS_DEFINITION_KEY,
                         ExecutionContextMessageHeaders.PROCESS_INSTANCE_ID,
                         ExecutionContextMessageHeaders.BUSINESS_KEY)
                     .map(headers::get)
                     .map(Optional::ofNullable)
                     .map(this::mapNullOrEmptyValue)
                     .collect(Collectors.joining("."));
    }
    
    private String mapNullOrEmptyValue(Optional<Object> obj) {
        return obj.map(Object::toString)
                  .filter(value -> !value.isEmpty())
                  .map(this::escapeIllegalCharacters)
                  .orElse(UNDERSCORE);
    }
    
    private String escapeIllegalCharacters(String value) {
        return value.replaceAll("[\\t\\s\\.*#:]", "-");
    }

}
