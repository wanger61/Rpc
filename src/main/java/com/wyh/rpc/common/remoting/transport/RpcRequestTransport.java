package com.wyh.rpc.common.remoting.transport;

import com.wyh.rpc.common.remoting.dto.RpcRequest;

//该接口用于发送Rpc请求并获取结果
public interface RpcRequestTransport {

    Object sendRpcRequest(RpcRequest rpcRequest);

}
