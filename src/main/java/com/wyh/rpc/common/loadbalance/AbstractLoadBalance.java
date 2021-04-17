package com.wyh.rpc.common.loadbalance;

import java.net.UnknownHostException;
import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance{

    @Override
    public String selectServiceAddress(List<String> serviceAddress, String rpcServiceName) {
        if (serviceAddress == null || serviceAddress.size() == 0){
            return null;
        }
        if (serviceAddress.size() == 1){
            return serviceAddress.get(0);
        }
        try {
            return doSelect(serviceAddress, rpcServiceName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract String doSelect(List<String> serviceAddress, String rpcServiceName) throws UnknownHostException;
}
