package com.scenic.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 服务层慢调用监控切面。
 *
 * <p>统计 service 层每个方法的执行耗时，只记录不干预：超过阈值时打 WARN，
 * 便于在不接入 APM 的情况下定位慢查询（例如看板聚合、订单分页）。
 * 阈值由 {@code app.slow-call-threshold-ms} 配置，默认 500ms。</p>
 *
 * <p>切面在 finally 中统计耗时，业务异常照常向外抛出，不影响
 * {@code GlobalExceptionHandler} 的统一异常处理与 {@code @Transactional} 的提交/回滚。</p>
 */
@Slf4j
@Aspect
@Component
public class SlowCallLogAspect {

    @Value("${app.slow-call-threshold-ms:500}")
    private long thresholdMs;

    @Around("execution(* com.scenic.service..*(..))")
    public Object logSlowCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost >= thresholdMs) {
                log.warn("慢调用 {}.{} 耗时 {}ms（阈值 {}ms）",
                        joinPoint.getSignature().getDeclaringType().getSimpleName(),
                        joinPoint.getSignature().getName(),
                        cost,
                        thresholdMs);
            }
        }
    }
}