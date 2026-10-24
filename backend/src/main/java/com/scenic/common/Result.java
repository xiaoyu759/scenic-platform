package com.scenic.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一返回结果类
 * 所有接口都使用此格式返回数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码：200成功，其他失败
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功 - 无数据返回
     */
    public static <T> Result<T> success() {
        return new Result<>(StatusCode.SUCCESS, "操作成功", null);
    }

    /**
     * 成功 - 有数据返回
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(StatusCode.SUCCESS, "操作成功", data);
    }

    /**
     * 成功 - 自定义消息
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(StatusCode.SUCCESS, message, data);
    }

    /**
     * 失败 - 默认500错误
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(StatusCode.SERVER_ERROR, message, null);
    }

    /**
     * 失败 - 自定义状态码
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败 - 自定义状态码和数据
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> paramError(String message) {
        return new Result<>(StatusCode.PARAM_ERROR, message, null);
    }

    /**
     * 未登录
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(StatusCode.UNAUTHORIZED, message, null);
    }

    /**
     * 无权限
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(StatusCode.FORBIDDEN, message, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == StatusCode.SUCCESS;
    }
}