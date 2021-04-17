package com.wyh.rpc.common.registry;

import com.wyh.rpc.common.exception.RpcErrorMessage;
import com.wyh.rpc.common.exception.RpcException;
import com.wyh.rpc.common.loadbalance.LoadBalance;
import com.wyh.rpc.common.registry.util.CuratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
@Component
public class ZkServiceDiscovery implements ServiceDiscovery {

    @Autowired
    private LoadBalance loadBalance;

    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {

        CuratorFramework zkClient = CuratorUtil.getClient();
        List<String> serviceAddressList = CuratorUtil.getServiceAddress(zkClient, rpcServiceName);
        if (serviceAddressList == null || serviceAddressList.size() == 0){
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        //通过负载均衡策略选择一个地址
        String targetServiceAddress = loadBalance.selectServiceAddress(serviceAddressList, rpcServiceName);;
        log.info("Successfully found the service address:[{}]", targetServiceAddress);
        String[] split = targetServiceAddress.split(":");
        String host = split[0];
        Integer port = Integer.parseInt(split[1]);
        return new InetSocketAddress(host, port);
    }
}
