package com.wyh.rpc.common.remoting.transport.client;

import com.wyh.rpc.common.remoting.dto.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于存放已发送但未收到回复的请求
 */
@Component
public class UnprocessedRequests {

    private final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_REQUEST_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future){
        UNPROCESSED_REQUEST_FUTURES.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_REQUEST_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException();
        }
    }
}
