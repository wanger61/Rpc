package com.wyh.rpc.common.exception;

public enum RpcErrorMessage {
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_CAN_NOT_BE_FOUND("未发现该服务"),
    REQUEST_NOT_MATCH_RESPONSE("返回结果错误！请求和返回的相应不匹配")
    ;

    private final String message;

    RpcErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
