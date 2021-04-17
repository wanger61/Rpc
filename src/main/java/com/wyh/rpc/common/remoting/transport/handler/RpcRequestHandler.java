package com.wyh.rpc.common.remoting.transport.handler;

import com.wyh.rpc.common.exception.RpcException;
import com.wyh.rpc.common.provider.ServiceProvider;
import com.wyh.rpc.common.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Component
public class RpcRequestHandler {

    @Autowired
    private ServiceProvider serviceProvider;

    public Object handle(RpcRequest rpcRequest){
        //通过服务提供方获取服务实现类
        Object service = serviceProvider.getService(rpcRequest.toRpcProperties());
        return invokeTargetService(service, rpcRequest);
    }

    /**
     * 利用反射调用方法：
     *      先根据方法名和参数类型获取相应方法
     *      再传入参数调用该方法
     * 返回方法结果
     */
    private Object invokeTargetService(Object service, RpcRequest rpcRequest) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
