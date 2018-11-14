package org.activiti.cloud.services.rest.conf;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "spring.activiti.serializePOJOsInVariablesToJson",
            havingValue = "true",
            matchIfMissing=true)
    public Jackson2ObjectMapperBuilderCustomizer configureJackson() {

        return jackson2ObjectMapperBuilder -> {

            TypeResolverBuilder<?> typeResolver = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
            typeResolver = typeResolver.init(JsonTypeInfo.Id.CLASS, null);
            typeResolver = typeResolver.inclusion(JsonTypeInfo.As.PROPERTY);

            jackson2ObjectMapperBuilder.defaultTyping(typeResolver);
        };
    }
}
