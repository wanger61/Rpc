package com.wyh.rpc.common.remoting.transport.client;

import com.wyh.rpc.ApplicationContextUtil;
import com.wyh.rpc.common.compress.CompressType;
import com.wyh.rpc.common.remoting.constant.RpcMessageType;
import com.wyh.rpc.common.remoting.dto.RpcMessage;
import com.wyh.rpc.common.remoting.dto.RpcResponse;
import com.wyh.rpc.common.serialize.SerializationType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;


@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private UnprocessedRequests unprocessedRequests;

    public NettyRpcClientHandler() {
        ApplicationContext applicationContext = ApplicationContextUtil.getApplicationContext();
        unprocessedRequests = applicationContext.getBean(UnprocessedRequests.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage msg) throws Exception {
        log.info("client receive msg: [{}]", msg);
        if (msg.getMessageType() == RpcMessageType.HEARTBEAT_PONG_TYPE.getCode()){
            log.info("heart [{}]", msg.getData());
        }else {
            RpcResponse<Object> response = (RpcResponse<Object>) msg.getData();
            //收到回复，在unprocessedRequest中移除请求，并将结果放入future中，调用方就可通过该future获取rpc调用结果了
            unprocessedRequests.complete(response);
        }
    }

    //处理心跳事件，如果5s未写则发送ping消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE){
                //log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                RpcMessage rpcMsg = RpcMessage.builder()
                        .compress(CompressType.GZIP.getCode())
                        .codec(SerializationType.KYRO.getCode())
                        .data("ping")
                        .messageType(RpcMessageType.HEARTBEAT_PING_TYPE.getCode())
                        .build();
                ctx.writeAndFlush(rpcMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
