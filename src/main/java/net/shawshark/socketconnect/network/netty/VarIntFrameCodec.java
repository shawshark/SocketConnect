package net.shawshark.socketconnect.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.CharsetUtil;

import java.util.List;

public class VarIntFrameCodec  extends ByteToMessageCodec<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        for(int i = 0; i < 3; i++) {
            if(!in.isReadable(i + 1)) {
                in.resetReaderIndex();
                return;
            }
            if(in.getByte(in.readerIndex() + i) < 0) {
                continue;
            }
            int size = readVarInt(in);
            if(size > in.readableBytes()) {
                in.resetReaderIndex();
                return;
            }
            out.add(in.readBytes(size));
            return;
        }
        throw new CorruptedFrameException("VarInt size is longer than 21-bit");
    }

    @Override
    protected void encode(ChannelHandlerContext context, ByteBuf in, ByteBuf out) throws Exception {
        writeVarInt(out, in.readableBytes());
        out.writeBytes(in);
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
