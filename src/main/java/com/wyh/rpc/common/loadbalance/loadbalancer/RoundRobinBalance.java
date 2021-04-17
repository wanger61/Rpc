package com.wyh.rpc.common.loadbalance.loadbalancer;

import com.wyh.rpc.common.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalance extends AbstractLoadBalance {

    private static int index = 0;

    @Override
    protected synchronized String doSelect(List<String> serviceAddress, String rpcServiceName) {
        if (index >= serviceAddress.size()){
            index = 0;
        }
        return serviceAddress.get(index++);
    }
}
