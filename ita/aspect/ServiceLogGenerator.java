package ita.aspect;

import io.micrometer.tracing.Tracer;
import ita.enumeration.EntityType;
import ita.enumeration.OperationType;
import ita.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static ita.enumeration.EntityType.*;
import static ita.enumeration.OperationType.READ_OPERATION;

@Aspect
@Component
@Slf4j
public class ServiceLogGenerator {
    private final String serviceName;
    private final Tracer tracer;

    public ServiceLogGenerator(
                               Tracer tracer,
                               @Value("${spring.application.name}" ) String serviceName) {
        this.tracer = tracer;
        this.serviceName = serviceName;
    }

    @Pointcut("@annotation(ita.aspect.GenerateServiceLog)")
    public void generateServiceLogAnnotation() {}

    @AfterReturning(pointcut = "generateServiceLogAnnotation()", returning = "returnValue")
    public void generateAuditLog(JoinPoint joinPoint, Object returnValue) throws NoSuchMethodException {
        if (returnValue == null){
            return;
           // throw new  IllegalArgumentException("returnValue can't be null");
        }

        String entityDetail = returnValue.toString();

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

        String name = AuthUtil.getUsername();

        MDC.put("user", name);

        log.info(entityDetail);
    }

    private EntityType getAnnotationEntityType(JoinPoint joinPoint) throws NoSuchMethodException {
        GenerateServiceLog generateServiceLog = getGenerateServiceLogAnnotation(joinPoint);

        EntityType entityType = generateServiceLog.entityType();

        return entityType != null ? entityType : NULL_TYPE;
    }

    private OperationType getAnnotationOperationType(JoinPoint joinPoint) throws NoSuchMethodException {
        GenerateServiceLog generateServiceLog = getGenerateServiceLogAnnotation(joinPoint);

        OperationType operationType = generateServiceLog.operationType();

        return operationType != null ? operationType : READ_OPERATION;
    }

    private GenerateServiceLog getGenerateServiceLogAnnotation(JoinPoint joinPoint) throws NoSuchMethodException {
        return joinPoint
                .getSignature()
                .getDeclaringType()
                .getMethod(joinPoint.getSignature().getName(), getParameterType(joinPoint))
                .getAnnotation(GenerateServiceLog.class);
    }

    private Class<?>[] getParameterType(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
    }

}
