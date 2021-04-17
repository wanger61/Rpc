package com.wyh.rpc.common.serialize;

import lombok.Getter;

@Getter
public enum  SerializationType {
    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff");


    private final byte code;

    private final String name;

    SerializationType(byte code, String name) {
        this.code = code;
        this.name = name;
    }
}
