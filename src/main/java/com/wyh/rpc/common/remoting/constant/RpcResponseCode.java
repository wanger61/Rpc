package com.wyh.rpc.common.remoting.constant;

import lombok.Getter;

public enum RpcResponseCode {
    SUCCESS(200, "OK"),
    FAIL(500, "Failed")
    ;

    private final Integer code;

    private final String message;

    RpcResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
