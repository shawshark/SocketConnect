package net.shawshark.socketconnect.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.shawshark.socketconnect.network.SocketConnect;
import net.shawshark.socketconnect.objects.Message;
import net.shawshark.socketconnect.objects.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EchoServer implements Runnable {

    @Getter private SocketConnect socketConnect;

    @Getter private Thread thread;

    private NioEventLoopGroup eventGroup;
    private Channel channel;

    private boolean closed = false;

    @Getter public EchoServer server;

    public EchoServer(SocketConnect socketConnect) {
        this.socketConnect = socketConnect;
        this.server = this;

        thread = new Thread(this);
        thread.setName("Echo server");
        thread.start();

    }

    private NioEventLoopGroup parentEventGroup;
    private NioEventLoopGroup childEventGroup;
    private PingKeepChannelsAlive pingTask;

    public void connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        this.parentEventGroup = new NioEventLoopGroup();
        this.childEventGroup = new NioEventLoopGroup();

        this.pingTask = new PingKeepChannelsAlive();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new VarIntFrameCodec());
                            channel.pipeline().addLast(new MessageEncoder());
                            channel.pipeline().addLast(new MessageDecoder());
                            channel.pipeline().addLast(new EchoServerHandler(getServer(), getSocketConnect().getBridgeConnect()));
                        }
                    });
            ChannelFuture future = bootstrap.bind(getSocketConnect().getIpAddress(), getSocketConnect().getPort()).sync();
            channel = future.channel();

            System.out.println("[Socket Connect] started and listening for connections on " + channel.localAddress());

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    @SneakyThrows
    public void sendMessage(Message message) {
        if(message != null) {
            for(Session session : ChannelCache.getGetSessionByUsername().values()) {
                //session.write(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
                //session.write(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
                session.getChannel().writeAndFlush(message);
            }
        }
    }

    @Deprecated
    public void writingToChannel(String message) {

        if(!isConnected()) {
            System.out.println("Unable to send request to server.");
            return;
        }

        Channel channel = this.channel; // Get the channel reference from somewhere
        ByteBuf buf = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
        ChannelFuture cf = channel.writeAndFlush(buf);
        cf.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
            } else {
                System.err.println("Write error");
                future.cause().printStackTrace();
            }
        });
    }

    public void disconnect() {
        try {
            if(channel != null && channel.isOpen()) {
                channel.close().sync();
            }

            if(eventGroup != null) {
                eventGroup.shutdownGracefully().sync();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventGroup = null;
            channel = null;
            closed = true;
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    @SneakyThrows
    @Override
    public void run() {

        while(!isConnected()) {
            try {
                connect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            while(isConnected()) {
                Thread.sleep(1000);
            }

            System.out.println("[Socket Connect] Lost connection, Starting up server...!");
        }
    }
}
