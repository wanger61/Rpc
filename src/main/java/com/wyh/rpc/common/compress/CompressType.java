package com.wyh.rpc.common.compress;

import lombok.Getter;

@Getter
public enum CompressType {

    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    CompressType(byte code, String name) {
        this.code = code;
        this.name = name;
    }
}
