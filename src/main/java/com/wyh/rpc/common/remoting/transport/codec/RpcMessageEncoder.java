package com.wyh.rpc.common.remoting.transport.codec;

import com.wyh.rpc.common.compress.GzipCompress;
import com.wyh.rpc.common.remoting.constant.RpcConstants;
import com.wyh.rpc.common.remoting.constant.RpcMessageType;
import com.wyh.rpc.common.remoting.dto.RpcMessage;
import com.wyh.rpc.common.serialize.KyroSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B codec（序列化类型） 1B compress（压缩类型）  4B  requestId（请求的Id）
 * body（object类型数据）
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private final AtomicInteger REQUESTID = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out) throws Exception {

        out.writeBytes(RpcConstants.MAGIC_NUMBER);
        out.writeByte(RpcConstants.VERSION);
        //预留长度字节
        out.writerIndex(out.writerIndex() + 4);
        byte messageType = rpcMessage.getMessageType();
        out.writeByte(messageType);
        out.writeByte(rpcMessage.getCodec());
        out.writeByte(rpcMessage.getCompress());
        out.writeInt(REQUESTID.getAndIncrement());

        byte[] bodyBytes = null;
        int fullLength = RpcConstants.HEAD_LENGTH;

        //如果不是心跳消息，则对数据进行序列化和压缩
        if (messageType != RpcMessageType.HEARTBEAT_PING_TYPE.getCode() &&
            messageType != RpcMessageType.HEARTBEAT_PONG_TYPE.getCode()){
            Object data = rpcMessage.getData();
            //序列化
            KyroSerializer kyroSerializer = new KyroSerializer();
            byte[] serialize = kyroSerializer.serialize(data);
            //数据压缩
            GzipCompress gzipCompress = new GzipCompress();
            bodyBytes = gzipCompress.compress(serialize);
            fullLength += bodyBytes.length;
        }

        //是心跳消息则不用发送消息体
        if (bodyBytes != null){
            out.writeBytes(bodyBytes);
        }
        //将总长度写入长度字段
        int writeIndex = out.writerIndex();
        out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
        out.writeInt(fullLength);
        out.writerIndex(writeIndex);
        //log.info("消息总长度："+ out.readableBytes());
    }
}
