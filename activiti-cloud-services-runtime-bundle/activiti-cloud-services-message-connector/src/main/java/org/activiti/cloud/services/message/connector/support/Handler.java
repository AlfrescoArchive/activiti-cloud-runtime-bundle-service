package org.activiti.cloud.services.message.connector.support;

@FunctionalInterface
public interface Handler<T, R> {
    R handle(T t);
}
