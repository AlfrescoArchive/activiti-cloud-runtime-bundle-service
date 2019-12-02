package org.activiti.cloud.services.message.connector.support;

@FunctionalInterface
public interface Chain<T, R> {
    R handle(T t);
}
