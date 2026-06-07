package com.huolala.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器内部错误"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),
    PARAM_VALID_ERROR(1001, "参数校验失败"),
    BUSINESS_ERROR(2001, "业务处理异常"),
    REDIS_ERROR(3001, "Redis操作异常");

    private final Integer code;
    private final String message;
}
