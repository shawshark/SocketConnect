package net.shawshark.socketconnect.bukkit.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.shawshark.socketconnect.BridgeConnect;
import net.shawshark.socketconnect.objects.AuthEvent;
import net.shawshark.socketconnect.objects.Message;
import net.shawshark.socketconnect.objects.Utils;
import net.shawshark.socketconnect.request.Listener;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<Message> {

    @Getter private BridgeConnect connect;
    public EchoClientHandler(BridgeConnect connect) {
        this.connect = connect;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        new BukkitRunnable() {
            @SneakyThrows
            @Override
            public void run() {
                //register
                Message message = new Message(String.valueOf(AuthEvent.getId()),
                        "c","c", "");
                message.setFromServer(getConnect().getNetwork().getUsername());
                ctx.channel().writeAndFlush(message);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugins()[0], 50);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {

        try {
            if(message != null) {

                int id = Integer.parseInt(message.getId());
                if(id == 1) {//message Event

                    // Fire all listeners
                    for(Listener listener : getConnect().getListeners()) {
                        listener.execute(message);
                    }

                }
                else if(id ==2) {
                    // ignore auth packet
                }
                else if(id == 3) {
                    // ignore
                }

                else {
                    System.out.println("[Socket Connect] Failed to read request, Unknown packet id: " + id);
                }

            } else {
                System.out.println("[Socket Connect] Failed to decode message!");
            }
        } catch (Exception e) {
            System.out.println("Failed to read request!");
            System.out.println("Request: " + message);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}