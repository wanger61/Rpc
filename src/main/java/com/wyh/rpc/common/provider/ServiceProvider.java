package com.wyh.rpc.common.provider;

public interface ServiceProvider {

    void publishService(Object service);

    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    Object getService(RpcServiceProperties rpcServiceProperties);

}
