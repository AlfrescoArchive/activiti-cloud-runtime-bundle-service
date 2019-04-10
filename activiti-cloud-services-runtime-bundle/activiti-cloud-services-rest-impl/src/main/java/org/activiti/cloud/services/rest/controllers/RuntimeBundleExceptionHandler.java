package org.activiti.cloud.services.rest.controllers;

import org.activiti.api.model.shared.model.ActivitiError;
import org.activiti.api.runtime.model.impl.ActivitiErrorImpl;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.cloud.services.core.ActivitiForbiddenException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.image.exception.ActivitiInterchangeInfoNotFoundException;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class RuntimeBundleExceptionHandler {

    @ExceptionHandler(ActivitiObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Resource<ActivitiError> handleAppException(ActivitiObjectNotFoundException ex, HttpServletResponse response) {
        response.setContentType("application/json");
        return new Resource<>(new ActivitiErrorImpl(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(ActivitiInterchangeInfoNotFoundException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String handleAppException(ActivitiInterchangeInfoNotFoundException ex, HttpServletResponse response) {
        response.setContentType("application/json");
        return ex.getMessage();
    }

    @ExceptionHandler(ActivitiForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Resource<ActivitiError> handleAppException(ActivitiForbiddenException ex, HttpServletResponse response) {
        response.setContentType("application/json");
        return new Resource<>(new ActivitiErrorImpl(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Resource<ActivitiError> handleAppException(NotFoundException ex, HttpServletResponse response) {
        response.setContentType("application/json");
        return new Resource<>(new ActivitiErrorImpl(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }
}
