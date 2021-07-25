package net.shawshark.socketconnect.bukkit.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import net.shawshark.socketconnect.bukkit.SocketConnectPlugin;
import net.shawshark.socketconnect.network.netty.MessageDecoder;
import net.shawshark.socketconnect.network.netty.MessageEncoder;
import net.shawshark.socketconnect.network.netty.VarIntFrameCodec;
import net.shawshark.socketconnect.objects.Message;
import net.shawshark.socketconnect.objects.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EchoClient implements Runnable {

    @Getter private String ipAddress;
    @Getter private int port;

    @Getter private SocketConnectPlugin socketConnect;

    @Getter private Thread thread;

    public Channel channel;
    public ChannelFuture channelFuture = null;

    int rcvBuf, sndBuf, lowWaterMark, highWaterMark;

    public EchoClient(SocketConnectPlugin connect) {
        socketConnect = connect;
        rcvBuf = Integer.MAX_VALUE;
        sndBuf = Integer.MAX_VALUE;
        lowWaterMark = 2048;
        highWaterMark = 3048;

        port = connect.getPort();
        ipAddress = connect.getIpAddress();

        thread = new Thread(this);
        thread.setName("Netty thread (Client)");
    }

    public void startClient() {
        thread.start();
    }

    public void stopClient() {
        thread.interrupt();

        if(channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    @Override
    public void run() {

        while(!isConnected()) {
            try {
                connect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            while(isConnected()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("[Socket Connect] Lost connection, Starting up server...!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public final void connect() throws InterruptedException {
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            Bootstrap bs = new Bootstrap();
            bs.group(workGroup);
            bs.channel(NioSocketChannel.class);
            bs.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_RCVBUF, rcvBuf)
                    .option(ChannelOption.SO_SNDBUF, sndBuf)
                    .option(ChannelOption.SO_LINGER, 0)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(lowWaterMark, highWaterMark))
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {

                            socketChannel.pipeline().addLast(new VarIntFrameCodec());
                            socketChannel.pipeline().addLast(new MessageEncoder());
                            socketChannel.pipeline().addLast(new MessageDecoder());
                            socketChannel.pipeline().addLast(new EchoClientHandler(getSocketConnect().getBridgeConnect()));
                        }
                    });

            channelFuture = bs.connect(getIpAddress(), getPort()).sync();
            this.channel = channelFuture.channel();
            channelFuture.channel().closeFuture().sync(); // wait's for channel to close

        } catch (Exception ex) {
            workGroup.shutdownGracefully();
        }
    }

    @SneakyThrows
    public void sendMessage(Message message) {
        if (message != null) {
            if(!isConnected()) {
                System.out.println("[Socket Connect] Failed to send message to server, Client isn't connect to the Server.");
            } else {
                //channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(data.getBytes()));
                //channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(data.getBytes(StandardCharsets.UTF_8)));
                channelFuture.channel().writeAndFlush(message);
            }
        } else {
            System.out.println("[Socket Connect] Message attempted to be sent to the server but the message is null?");
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }
}
