package ita.aspect;

import io.micrometer.tracing.Tracer;
import ita.enumeration.EntityType;
import ita.enumeration.OperationType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

import static ita.enumeration.EntityType.NULL_TYPE;
import static ita.enumeration.OperationType.READ_OPERATION;

@Aspect
@Component
@Slf4j
public class RequestLogGenerator {

    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    private Tracer tracer;

    @Pointcut("@annotation(ita.aspect.GenerateRequestLog)")
    public void generateAuditLogAnnotation() {}

    @Before("execution(* ita.controller.*.*(.., @org.springframework.web.bind.annotation.RequestBody (*), ..))")
    public void logRequest(JoinPoint joinPoint) throws NoSuchMethodException {

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

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                log.info("Request Body: {}", arg);
                return;
            }
        }
    }

    private EntityType getAnnotationEntityType(JoinPoint joinPoint) throws NoSuchMethodException {
        GenerateRequestLog generateRequestLog = getGenerateRequestLogAnnotation(joinPoint);

        EntityType entityType = generateRequestLog.entityType();

        return entityType != null ? entityType : NULL_TYPE;
    }

    private OperationType getAnnotationOperationType(JoinPoint joinPoint) throws NoSuchMethodException {
        GenerateRequestLog generateRequestLog = getGenerateRequestLogAnnotation(joinPoint);

        OperationType operationType = generateRequestLog.operationType();

        return operationType != null ? operationType : READ_OPERATION;
    }

    private GenerateRequestLog getGenerateRequestLogAnnotation(JoinPoint joinPoint) throws NoSuchMethodException {
        return joinPoint
                .getSignature()
                .getDeclaringType()
                .getMethod(joinPoint.getSignature().getName(), getParameterType(joinPoint))
                .getAnnotation(GenerateRequestLog.class);
    }

    private Class<?>[] getParameterType(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
    }

}
