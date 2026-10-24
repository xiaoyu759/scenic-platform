package com.scenic.exception;

import com.scenic.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * <p>统一把异常收敛成 {@link Result} 结构，避免异常堆栈直接暴露给前端；
 * 同时把「业务失败」与「系统故障」分流：前者只记 warn，后者记 error 并保留堆栈便于排查。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：可预期，只记 warn，不打印堆栈
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务处理失败: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验失败：取第一条字段错误信息返回，前端可直接展示
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleValidationException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return Result.paramError(message);
    }

    /**
     * 兜底处理
     *
     * <p>Spring MVC 自身的标准异常（404、405 等实现了 {@link ErrorResponse}）保留原始 HTTP 状态码，
     * 其余未知异常统一返回「系统繁忙」，真实原因只进服务端日志。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            log.warn("请求处理失败: status={}, message={}", status.value(), e.getMessage());
            return ResponseEntity.status(status).body(Result.error(status.value(), e.getMessage()));
        }
        log.error("系统异常", e);
        return ResponseEntity.ok(Result.error("系统繁忙，请稍后重试"));
    }
}