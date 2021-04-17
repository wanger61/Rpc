package com.wyh.rpc.common.loadbalance.loadbalancer;

import com.wyh.rpc.common.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddress, String rpcServiceName) {
        Random random = new Random();
        return serviceAddress.get(random.nextInt(serviceAddress.size()));
    }
}
