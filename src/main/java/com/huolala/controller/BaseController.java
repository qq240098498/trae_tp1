package com.huolala.controller;

import com.huolala.common.Result;
import com.huolala.common.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BaseController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected HttpSession session;

    protected <T> Result<T> success() {
        return Result.success();
    }

    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    protected <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    protected <T> Result<T> error() {
        return Result.error();
    }

    protected <T> Result<T> error(String message) {
        return Result.error(message);
    }

    protected <T> Result<T> error(Integer code, String message) {
        return Result.error(code, message);
    }

    protected <T> Result<T> error(ResultCode resultCode) {
        return Result.error(resultCode);
    }

    protected Long getCurrentUserId() {
        Object userId = session.getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }

    protected String getCurrentUsername() {
        Object username = session.getAttribute("username");
        return username != null ? (String) username : null;
    }

    protected String getCurrentUserRole() {
        Object role = session.getAttribute("role");
        return role != null ? (String) role : null;
    }

    protected boolean isLogin() {
        return getCurrentUserId() != null;
    }

    protected String getParameter(String name) {
        return request.getParameter(name);
    }

    protected String getParameter(String name, String defaultValue) {
        String value = request.getParameter(name);
        return value != null ? value : defaultValue;
    }

    protected Integer getParameterInt(String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Integer getParameterInt(String name, Integer defaultValue) {
        Integer value = getParameterInt(name);
        return value != null ? value : defaultValue;
    }

    protected Long getParameterLong(String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Long getParameterLong(String name, Long defaultValue) {
        Long value = getParameterLong(name);
        return value != null ? value : defaultValue;
    }

    protected Double getParameterDouble(String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Double getParameterDouble(String name, Double defaultValue) {
        Double value = getParameterDouble(name);
        return value != null ? value : defaultValue;
    }

    protected Boolean getParameterBoolean(String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    protected Boolean getParameterBoolean(String name, Boolean defaultValue) {
        Boolean value = getParameterBoolean(name);
        return value != null ? value : defaultValue;
    }

    protected void setSessionAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    protected Object getSessionAttribute(String name) {
        return session.getAttribute(name);
    }

    protected void removeSessionAttribute(String name) {
        session.removeAttribute(name);
    }

    protected String getRequestURI() {
        return request.getRequestURI();
    }

    protected String getRequestMethod() {
        return request.getMethod();
    }

    protected String getRemoteAddr() {
        return request.getRemoteAddr();
    }
}
