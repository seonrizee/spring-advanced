package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.AuthorizationException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        authorizeAdmin(request);

        Long userId = (Long) request.getAttribute("userId");
        log.info("[ADMIN_요청_성공] Method={}, URI={}, UserID={}",
                request.getMethod(),
                request.getRequestURI(),
                userId);

        return true;
    }

    private void authorizeAdmin(HttpServletRequest request) {

        String userRoleString = request.getAttribute("userRole").toString();
        if (userRoleString == null) {
            throw new AuthorizationException("권한 정보가 없는 요청입니다.");
        }

        UserRole userRole = UserRole.of(userRoleString);
        if (!UserRole.ADMIN.equals(userRole)) {
            throw new AuthorizationException("접근 권한이 없습니다.");
        }
    }
}
