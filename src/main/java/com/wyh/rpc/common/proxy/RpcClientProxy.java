package com.wyh.rpc.common.proxy;

import com.wyh.rpc.common.exception.RpcErrorMessage;
import com.wyh.rpc.common.exception.RpcException;
import com.wyh.rpc.common.provider.RpcServiceProperties;
import com.wyh.rpc.common.remoting.constant.RpcResponseCode;
import com.wyh.rpc.common.remoting.dto.RpcRequest;
import com.wyh.rpc.common.remoting.dto.RpcResponse;
import com.wyh.rpc.common.remoting.transport.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final NettyClient nettyClient;

    private final RpcServiceProperties rpcServiceProperties;

    public RpcClientProxy(NettyClient nettyClient, RpcServiceProperties rpcServiceProperties) {
        this.nettyClient = nettyClient;
        if (rpcServiceProperties.getGroup() == null){
            rpcServiceProperties.setGroup("");
        }
        if (rpcServiceProperties.getVersion() == null){
            rpcServiceProperties.setVersion("");
        }
        this.rpcServiceProperties = rpcServiceProperties;
    }

    /**
     * 获取代理对象
     * @param clazz 传入服务接口class
     */
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     *  调用代理对象的方法实际会去调用以下逻辑
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = nettyClient.sendRpcRequest(rpcRequest).get();
        check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }

    /**
     * 检查RpcRequest是否合法
     */
    private void check(RpcRequest rpcRequest, RpcResponse<Object> rpcResponse) {
        if (rpcResponse == null){
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())){
            throw new RpcException(RpcErrorMessage.REQUEST_NOT_MATCH_RESPONSE, "interfaceName:" + rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
        }
    }
}
