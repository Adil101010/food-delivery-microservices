package com.fooddelivery.userservice.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestContextUtil {

    public static String getUserIdFromHeader() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("X-User-Id") : null;
    }

    public static String getUserEmailFromHeader() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("X-User-Email") : null;
    }

    public static String getUserRoleFromHeader() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("X-User-Role") : null;
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
