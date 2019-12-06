package org.activiti.cloud.services.message.connector.support;

import java.util.function.Predicate;

public class Predicates {

    public static <T> Predicate<T> predicate(Predicate<T> predicate) {
        return predicate;
    }    
    public static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }
    
}
