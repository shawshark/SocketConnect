package net.shawshark.socketconnect.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import net.shawshark.socketconnect.objects.Message;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    public MessageEncoder() {}

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        String id = message.getId();
        String msg = message.getMessage();
        String channel = message.getChannel();
        String from = message.getFromServer();
        String forServers = message.getToServers();

        //writeVarInt(byteBuf, id);
        writeString(byteBuf, id);
        writeString(byteBuf, msg);
        writeString(byteBuf, channel);
        writeString(byteBuf, from);
        writeString(byteBuf, forServers);
    }

    public static int readVarInt(ByteBuf buffer) {
        int value = 0;
        int bytes = 0;
        byte in;
        while(true) {
            in = buffer.readByte();
            value |= (in & 0x7F) << (bytes++ * 7);
            if(bytes > 32) {
                throw new IllegalArgumentException("VarInt is too long: " + bytes);
            }
            if((in & 0x80) == 0x80) {
                continue;
            }
            break;
        }
        return value;
    }

    public static void writeVarInt(ByteBuf buffer, int value) {
        byte in;
        while(true) {
            in = (byte) (value & 0x7F);
            value >>>= 7;
            if(value != 0) {
                in |= 0x80;
            }
            buffer.writeByte(in);
            if(value != 0) {
                continue;
            }
            break;
        }
    }

    public static String readString(ByteBuf buffer) {
        byte[] bytes = new byte[readVarInt(buffer)];
        buffer.readBytes(bytes);
        return new String(bytes, CharsetUtil.UTF_8);
    }

    public static void writeString(ByteBuf buffer, String string) {
        byte[] bytes = string.getBytes(CharsetUtil.UTF_8);
        writeVarInt(buffer, bytes.length);
        buffer.writeBytes(bytes);
    }

    public static byte[] readBytes(ByteBuf from, int length) {
        byte[] data = new byte[length];
        from.readBytes(data);
        return data;
    }
}
