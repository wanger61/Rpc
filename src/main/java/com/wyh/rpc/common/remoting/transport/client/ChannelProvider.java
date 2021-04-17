package com.wyh.rpc.common.remoting.transport.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类用于管理连接
 */
@Slf4j
@Component
public class ChannelProvider {

    //key为服务提供方的地址
    private Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        if (channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            //判断该连接是可用的，若不可用则从map中移除
            if (channel != null && channel.isActive()){
                return channel;
            }else {
                channelMap.remove(channel);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel){
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    public void remove(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        if (channelMap.containsKey(key)){
            channelMap.remove(key);
        }
    }

}
