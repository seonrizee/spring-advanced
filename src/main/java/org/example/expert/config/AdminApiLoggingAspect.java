package org.example.expert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminApiLoggingAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(org.example.expert.domain.common.annotation.AdminLog)")
    public Object logAdminApiExecution(ProceedingJoinPoint pjp) throws Throwable {
        String traceId = UUID.randomUUID().toString();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        try {
            Map<String, Object> requestMap = buildLogInfo(request, getRequestBody(pjp), traceId);
            log.info("[ADMIN_API_요청][{}] {}", traceId, serializeLogData(requestMap));

            Object result = pjp.proceed();

            Map<String, Object> responseMap = buildLogInfo(request, result, traceId);
            log.info("[ADMIN_API_응답][{}] {}", traceId, serializeLogData(responseMap));

            return result;
        } catch (Exception e) {
            log.error("[ADMIN_API_AOP_오류][{}] URL: {}, Error: {}",
                    traceId, request.getRequestURL(), e.getMessage());
            throw e;
        }
    }

    private Map<String, Object> buildLogInfo(HttpServletRequest request, Object body, String traceId) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", LocalDateTime.now());
        map.put("traceId", traceId);
        map.put("method", request.getMethod());
        map.put("URL", String.valueOf(request.getRequestURL()));
        map.put("userId", request.getAttribute("userId"));
        map.put("requestBody", body);

        return map;
    }

    private Object getRequestBody(ProceedingJoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                return args[i];
            }
        }

        return null;
    }

    private String serializeLogData(Map<String, Object> logData) {
        try {
            return objectMapper.writeValueAsString(logData);
        } catch (JsonProcessingException e) {
            log.error("[ADMIN_API_AOP_오류]: JSON 직렬화 중 오류가 발생했습니다.", e);
            return "JSON 직렬화 중 오류가 발생했습니다.";
        }
    }
}
