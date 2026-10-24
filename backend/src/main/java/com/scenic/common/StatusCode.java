package com.scenic.common;

/**
 * 状态码常量
 */
public interface StatusCode {

    /**
     * 成功
     */
    int SUCCESS = 200;

    /**
     * 参数错误
     */
    int PARAM_ERROR = 400;

    /**
     * 未登录 / Token过期
     */
    int UNAUTHORIZED = 401;

    /**
     * 无权限
     */
    int FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    int NOT_FOUND = 404;

    /**
     * 请求方法不允许
     */
    int METHOD_NOT_ALLOWED = 405;

    /**
     * 请求冲突
     */
    int CONFLICT = 409;

    /**
     * 服务器内部错误
     */
    int SERVER_ERROR = 500;

    /**
     * 服务不可用
     */
    int SERVICE_UNAVAILABLE = 503;
}