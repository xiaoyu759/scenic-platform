package com.scenic.exception;

import com.scenic.common.StatusCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * <p>用于表达「可预期」的业务失败（参数非法、状态不允许、资源不存在等）。
 * 与系统异常区分开，由全局异常处理器转换为统一响应体，不打印堆栈。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务状态码，默认按参数错误处理
     */
    private final Integer code;

    public BusinessException(String message) {
        this(StatusCode.PARAM_ERROR, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}