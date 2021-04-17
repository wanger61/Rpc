package com.wyh.rpc.common.loadbalance;

import java.util.List;

public interface LoadBalance {

    String selectServiceAddress(List<String> serviceAddress, String rpcServiceName);

}
