package com.english.api.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by hungpham on 9/22/2025
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // ===== SERVICE =====
    @Before("execution(* com.english.api.*.service..*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        logBefore(joinPoint, "Service");
    }

    @AfterReturning("execution(* com.english.api.*.service..*(..))")
    public void logAfterReturningService(JoinPoint joinPoint) {
        logAfterReturning(joinPoint, "Service");
    }

    @Around("execution(* com.english.api.*.service..*(..))")
    public Object logExecutionTimeService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "Service");
    }

    // ===== CONTROLLER =====
    @Before("execution(* com.english.api.*.controller..*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        logBefore(joinPoint, "Controller");
    }

    @AfterReturning("execution(* com.english.api.*.controller..*(..))")
    public void logAfterReturningController(JoinPoint joinPoint) {
        logAfterReturning(joinPoint, "Controller");
    }

    @Around("execution(* com.english.api.*.controller..*(..))")
    public Object logExecutionTimeController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "Controller");
    }

    // ===== ERROR =====
    @AfterThrowing(value = "execution(* com.english.api.*..*(..))", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        String clientIp = getClientIp();
        String method = joinPoint.getSignature().toShortString();

        String logMessage = String.format("[ERROR] Method: %s | IP: %s | Exception: %s",
                method, clientIp, ex.getMessage());

        log.error(logMessage);
        saveLogToFile(logMessage);
    }

    // ===== Helper methods =====
    private void logBefore(JoinPoint joinPoint, String layer) {
        String clientIp = getClientIp();
        log.info("[{}] BEFORE: {} | IP: {}", layer, joinPoint.getSignature().toShortString(), clientIp);
    }

    private void logAfterReturning(JoinPoint joinPoint, String layer) {
        log.info("[{}] AFTER RETURNING: {}", layer, joinPoint.getSignature().toShortString());
    }

    private Object logExecutionTime(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        Instant start = Instant.now();
        Object result = joinPoint.proceed();
        Instant end = Instant.now();

        Duration duration = Duration.between(start, end);
        log.info("[{}] EXECUTED: {} | Time: {} ms",
                layer, joinPoint.getSignature().toShortString(), duration.toMillis());

        return result;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }

    private void saveLogToFile(String message) {
        try (FileWriter writer = new FileWriter("logs/app.log", true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            log.error("Could not write log to file", e);
        }
    }
}
