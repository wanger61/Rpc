package com.wyh.rpc.common.loadbalance.loadbalancer;

import com.wyh.rpc.common.loadbalance.AbstractLoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    //本地缓存，存放各个服务的Hash选择器
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, String rpcServiceName) throws UnknownHostException {
        //获取地址列表的hash值
        int identityHashCode = System.identityHashCode(serviceAddresses);
        //获取该服务的选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName);

        //如果Hash选择器未创建或地址列表已更新，则需要重新创建Hash选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }

        //利用Hash选择器获取一个地址
        return selector.select();
    }

    /**
     * Hash选择器
     * 内置有存放地址节点的Hash环，负责通过一致性Hash算法获取一个服务地址
     * 这里一致性Hash的Key选取本地IP,即相同的IP总会被负载到同一台服务器上
     */
    static class ConsistentHashSelector {
        //存放虚拟节点的哈希环
        private final TreeMap<Long, String> virtualInvokers;
        //hashCode
        private final int identityHashCode;
        //虚拟节点数
        private final int replicaNumber;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            this.replicaNumber = replicaNumber;

            //向Hash环中存放虚拟节点
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    //对address+i进行md5运算得到一个长度为16的字节数组
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        //h=0时，取digest中下标为0~3的4个字节进行位运算
                        //h=1时，取digest中下标为4~7的4个字节进行位运算
                        //h=2,h=3时过程同上
                        long m = hash(digest, h);
                        //将hash到invoker的映射关系存储到virtualInvokers中，
                        //virtualInvokers需要提供高效的查询操作，因此选用TreeMap作为存储结构
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select() throws UnknownHostException {
            String IPAddress = InetAddress.getLocalHost().getAddress().toString();
            //将本地的IP地址进行md5和hash运算后去哈希环中寻找对应的虚拟节点
            byte[] digest = md5(IPAddress);
            //取digest数组的前四个字节进行hash运算,再将hash值传给selectForKey方法，
            return selectForKey(hash(digest, 0));
        }

        //根据hash值在环上寻找所属的虚拟节点
        public String selectForKey(long hashCode) {
            //寻找大于等于该hash值的节点
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            //如果没有则循环至首节点
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
}
