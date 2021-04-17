package com.wyh.rpc.common.exception;

public class RpcException extends RuntimeException {

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(RpcErrorMessage rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
