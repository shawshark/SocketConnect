package net.shawshark.socketconnect.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import net.shawshark.socketconnect.BridgeConnect;
import net.shawshark.socketconnect.objects.Message;
import net.shawshark.socketconnect.objects.MessageEvent;
import net.shawshark.socketconnect.objects.Utils;
import net.shawshark.socketconnect.request.Listener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ChannelHandler.Sharable
public class EchoServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final AttributeKey<Session> sessions = AttributeKey.newInstance("session");

    @Getter private EchoServer server;
    @Getter private BridgeConnect connect;

    public EchoServerHandler(EchoServer server, BridgeConnect connect) {
        this.server = server;
        this.connect = connect;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        context.attr(sessions).setIfAbsent(new Session(context.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        Session session = context.attr(sessions).getAndSet(null);
        session.deregister();

        if(session.getUsername() != null) {
            System.out.println("[Socket Connect] " + session.getUsername() + " has gone offline. De-registering channel!");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        Channel channel = context.channel();
        if(cause instanceof IOException) {
            if(!cause.getMessage().equals("Connection reset by peer")) {
                cause.printStackTrace();
            }
        } else if (!(cause instanceof ReadTimeoutException)) {
            cause.printStackTrace();
        }
        if(channel.isOpen()) {
            channel.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Message message) throws Exception {

        int id = Integer.parseInt(message.getId());

        // If id is 2, let it past as it's the authing packet
        if(id != 2) {
            //check to see if channel has been authed
            Session session = context.attr(sessions).get();
            if(session == null || session.getRole() == Session.SessionAuthed.NOT_AUTHED) {
                System.out.println("[Socket Connect] Server has received packet from an un-authed channel, " +
                        "We are going to ignore this packet");
                return;
            }
        }

        System.out.println("[Socket Connect] Reading packet id: " + id + " from server: " + message.getFromServer());
        if(id == MessageEvent.getId()) {//message Event

            // send back to all servers
            for(Session session : ChannelCache.getGetSessionByUsername().values()) {

                // If this message is only meant for x server(s)
                // only send it to them (saves resources ?)
                if(!message.getToServers().equalsIgnoreCase("")) {
                    if(!message.containsUsername(session.getUsername())) {
                        continue;
                    }
                }

                session.write(message);
            }

            // Fire all listeners
            for(Listener listener : getConnect().getListeners()) {
                listener.execute(message);
            }
        }

        else if(id == 2) {

            context.attr(sessions).get().register(message.getFromServer());
            System.out.println("[Socket Connect] Authed username " + message.getFromServer());

        } else {
            System.out.println("[Socket Connect] Failed to read request, Unknown packet id: " + id);
        }
    }
}