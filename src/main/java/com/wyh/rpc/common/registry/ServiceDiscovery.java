package com.wyh.rpc.common.registry;

import java.net.InetSocketAddress;

/**
 * 返回指定服务的地址
 */
public interface ServiceDiscovery {

    InetSocketAddress lookupService(String rpcServiceName);
}
