package com.wyh.rpc.common.registry.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorUtil {


    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;

    private static final String ZKADDRESS = "10.92.224.90";
    private static final int PORT = 2181;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";

    //缓存已注册服务的地址
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //缓存所有注册地址
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private static CuratorFramework zkClient;

    private CuratorUtil(){}

    public static CuratorFramework getClient(){

        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZKADDRESS + ":" + PORT)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();

        try {
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        return zkClient;
    }

    public static void createPersistentNode(CuratorFramework zkClient, String path){
        try {
            //先查看缓存中是否已注册该服务
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("The node already exists. The node is:[{}]", path);
            }else {
                //eg: /my-rpc/github.javaguide.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        }catch (Exception e){
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    //删除该服务端的所有服务
    public static void clearRegistry(CuratorFramework zkClient, InetAddress inetAddress){
        //该操作较为耗时，可通过Stream流并行删除
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            }catch (Exception e){
                log.error("clear registry for path [{}] fail", p);
            }
            log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
        });
    }

    public static List<String> getServiceAddress(CuratorFramework zkClient, String rpcServiceName){
        //先从缓存中去取地址
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        //缓存中没有则请求Zookeeper获取服务地址
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;

        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(zkClient, rpcServiceName);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    private static void registerWatcher(CuratorFramework zkClient, String rpcServiceName) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache cache = new PathChildrenCache(zkClient, servicePath, true);
        cache.getListenable().addListener((curatorFramework, pathChildrenCacheEvent) -> {
            switch (pathChildrenCacheEvent.getType()){
                case CHILD_ADDED:
                    log.info("New Service Redistered:" + pathChildrenCacheEvent.getData());
                    break;
                case CHILD_REMOVED:
                    log.info("Service UnRedistered:" + pathChildrenCacheEvent.getData());
                    break;
                case CHILD_UPDATED:
                    log.info("Service Updated:" + pathChildrenCacheEvent.getData());
                    break;
            }
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        });
        cache.start();
    }


}
