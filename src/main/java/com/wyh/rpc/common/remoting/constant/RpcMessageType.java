package com.wyh.rpc.common.remoting.constant;

import lombok.Getter;

@Getter
public enum  RpcMessageType {
    REQUEST_TYPE((byte) 1),
    RESPONSE_TYPE((byte) 2),
    HEARTBEAT_PING_TYPE((byte) 3),
    HEARTBEAT_PONG_TYPE((byte) 4)
    ;

    private final byte code;

    RpcMessageType(byte code) {
        this.code = code;
    }
}
