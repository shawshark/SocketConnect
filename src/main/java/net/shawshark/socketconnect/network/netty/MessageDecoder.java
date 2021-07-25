package net.shawshark.socketconnect.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.shawshark.socketconnect.objects.Message;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    public MessageDecoder() {}

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(!byteBuf.isReadable()) return;

        String id = MessageEncoder.readString(byteBuf);
        String msg = MessageEncoder.readString(byteBuf);
        String channel = MessageEncoder.readString(byteBuf);
        String from = MessageEncoder.readString(byteBuf);
        String forServers = MessageEncoder.readString(byteBuf);

        //String id, String channel, String message, String... toServers
        Message message = new Message(id, channel, msg, forServers);
        message.setFromServer(from);
        list.add(message);
    }
}
