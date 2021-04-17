package com.wyh.rpc.common.remoting.transport.codec;

import com.wyh.rpc.common.compress.GzipCompress;
import com.wyh.rpc.common.remoting.constant.RpcConstants;
import com.wyh.rpc.common.remoting.constant.RpcMessageType;
import com.wyh.rpc.common.remoting.dto.RpcMessage;
import com.wyh.rpc.common.remoting.dto.RpcRequest;
import com.wyh.rpc.common.remoting.dto.RpcResponse;
import com.wyh.rpc.common.serialize.KyroSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * LengthFieldBasedFrameDecoder可以根据协议中的长度字段分帧，从而解决TCP粘包，半包问题
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     *
     * @param maxFrameLength 最大帧长，规定为1MB
     * @param lengthFieldOffset 长度域对偏移量
     * @param lengthFieldLength 长度域所占字节数
     * @param lengthAdjustment  长度补偿量，因为协议中规定的长度域为整个包的总长，因此需要对前面9个字节进行补偿
     * @param initialBytesToStrip 跳过的字节数
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf)decoded;
            if (frame.readableBytes() >= RpcConstants.HEAD_LENGTH){
                try {
                    return decodeFrame(frame);
                }catch (Exception e){
                    log.error("Decode frame error!", e);
                    throw e;
                }finally {
                    //释放缓冲区
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in){
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .compress(compressType)
                .requestId(requestId).build();
        //处理心跳消息
        if (messageType == RpcMessageType.HEARTBEAT_PONG_TYPE.getCode()) {
            rpcMessage.setData("PONG");
            return rpcMessage;
        }
        if (messageType == RpcMessageType.HEARTBEAT_PING_TYPE.getCode()) {
            rpcMessage.setData("PING");
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0){
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            //数据解压缩
            GzipCompress gzipCompress = new GzipCompress();
            bs = gzipCompress.decompress(bs);
            //序列化
            KyroSerializer kyroSerializer = new KyroSerializer();
            if (messageType == RpcMessageType.REQUEST_TYPE.getCode()){
                RpcRequest rpcRequest = kyroSerializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            }else {
                RpcResponse rpcResponse = kyroSerializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }
            return rpcMessage;
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcConstants.VERSION){
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] temp = new byte[len];
        in.readBytes(temp);
        for (int i=0; i<len; i++){
            if (temp[i] != RpcConstants.MAGIC_NUMBER[i]){
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(temp));
            }
        }
    }

}
