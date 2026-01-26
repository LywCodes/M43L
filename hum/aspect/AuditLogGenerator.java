package ita.aspect;

import io.micrometer.tracing.Tracer;
import ita.dto.JwtResponseDto;
import ita.dto.ResponseDto;
import ita.enumeration.EntityType;
import ita.enumeration.OperationType;
import ita.util.AuthUtil;
import ita.util.NetworkUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static ita.enumeration.EntityType.*;
import static ita.enumeration.OperationType.LOGIN_OPERATION;
import static ita.enumeration.OperationType.READ_OPERATION;

@Aspect
@Component
@Slf4j
public class AuditLogGenerator {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    private Tracer tracer;

    @Pointcut("@annotation(ita.aspect.GenerateAuditLog)")
    public void generateAuditLogAnnotation() {}

    @AfterReturning(pointcut = "generateAuditLogAnnotation()", returning = "returnValue")
    public void generateAuditLog(JoinPoint joinPoint, ResponseEntity<ResponseDto<Object>> returnValue) throws NoSuchMethodException {
        ResponseDto<Object> response = returnValue.getBody();

        assert response != null;

        String entityDetail;

        if (response.getOutputSchema().toString().contains("Page")) {
            Page<Object> pageObject = (Page<Object>) response.getOutputSchema();
            entityDetail = pageObject.getContent().toString();
        } else {
            entityDetail = response.getOutputSchema().toString();
        }

        EntityType entityType = getAnnotationEntityType(joinPoint);
        OperationType operationType = getAnnotationOperationType(joinPoint);

        MDC.remove("traceId");
        MDC.remove("spanId");

        MDC.put("entity_type", entityType.getValue());
        MDC.put("operation_type", operationType.getValue());
        MDC.put("service", serviceName);
        MDC.put("trace_id", Objects.requireNonNull(tracer.currentTraceContext().context()).traceId());
        MDC.put("method", joinPoint.getSignature().getName());
        MDC.put("actual_class", joinPoint.getTarget().getClass().getSimpleName());

        if (operationType.equals(LOGIN_OPERATION) && entityType.equals(AUTH_TYPE)) {
            MDC.put("user_address", httpServletRequest.getHeader("X-Original-Forwarded-For"));

            JwtResponseDto jwtResponseDto = (JwtResponseDto) response.getOutputSchema();

            MDC.put("user", jwtResponseDto.getUsername());
        }

        if (!entityType.equals(ERROR_TYPE) && !operationType.equals(LOGIN_OPERATION)) {
            String name = AuthUtil.getUsername();

            MDC.put("user", name);
        }

        log.info(String.format("Entity Detail: %s", entityDetail));
    }

    private EntityType getAnnotationEntityType(JoinPoint joinPoint) throws NoSuchMethodException {
        GenerateAuditLog generateAuditLog = getGenerateAuditLogAnnotation(joinPoint);

        EntityType entityType = generateAuditLog.entityType();

        return entityType != null ? entityType : NULL_TYPE;
    }

    private OperationType getAnnotationOperationType(JoinPoint joinPoint) throws NoSuchMethodException {
        GenerateAuditLog generateAuditLog = getGenerateAuditLogAnnotation(joinPoint);

        OperationType operationType = generateAuditLog.operationType();

        return operationType != null ? operationType : READ_OPERATION;
    }

    private GenerateAuditLog getGenerateAuditLogAnnotation(JoinPoint joinPoint) throws NoSuchMethodException {
        return joinPoint
                .getSignature()
                .getDeclaringType()
                .getMethod(joinPoint.getSignature().getName(), getParameterType(joinPoint))
                .getAnnotation(GenerateAuditLog.class);
    }

    private Class<?>[] getParameterType(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
    }

}
