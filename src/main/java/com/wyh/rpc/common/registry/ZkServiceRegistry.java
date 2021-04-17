package com.wyh.rpc.common.registry;

import com.wyh.rpc.common.registry.util.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class ZkServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetAddress) {
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName  + inetAddress.toString();
        CuratorFramework client = CuratorUtil.getClient();
        CuratorUtil.createPersistentNode(client, servicePath);
    }

}
