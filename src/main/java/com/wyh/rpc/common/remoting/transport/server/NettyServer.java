package com.wyh.rpc.common.remoting.transport.server;

import com.wyh.rpc.common.provider.RpcServiceProperties;
import com.wyh.rpc.common.provider.ServiceProvider;
import com.wyh.rpc.common.remoting.transport.codec.RpcMessageDecoder;
import com.wyh.rpc.common.remoting.transport.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyServer {

    public static final int PORT = 9998;

    @Autowired
    ServiceProvider serviceProvider;

    /**
     * 服务注册方法
     * @param service 服务的实现类
     * @param rpcServiceProperties  服务的属性（版本号+实现类标识group)
     */
    public void registerService(Object service, RpcServiceProperties rpcServiceProperties){
        serviceProvider.publishService(service, rpcServiceProperties);
    }

    public void registerService(Object service){
        serviceProvider.publishService(service);
    }

    @SneakyThrows  //该注解会抛出方法中的异常
    public void start(){

        String host = InetAddress.getLocalHost().getHostAddress();
        log.info(host);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(new NettyRpcServerHandler());
                        }
                    });

            ChannelFuture cf = serverBootstrap.bind(host, PORT).sync();
            cf.channel().closeFuture().sync();
        }catch (InterruptedException e){
            log.error("occur exception when start server:", e);
        }finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
