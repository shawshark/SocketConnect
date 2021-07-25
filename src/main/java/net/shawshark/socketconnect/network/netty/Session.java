package net.shawshark.socketconnect.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import net.shawshark.socketconnect.objects.Message;
import net.shawshark.socketconnect.objects.PingEvent;

public class Session {

    @Getter private Channel channel;
    @Getter private String username;

    @Getter@Setter private SessionAuthed role;

    public Session(Channel channel) {
        this.channel = channel;
        setRole(SessionAuthed.NOT_AUTHED);
    }

    public void register(String username) {

        this.username = username;
        setRole(SessionAuthed.AUTHED);
        ChannelCache.getGetSessionByUsername().put(username, this);
        System.out.println("[Socket Connect] Saved username " + username + " in memory");
    }

    public void deregister() {
        if(getUsername() != null) {
            ChannelCache.getGetSessionByUsername().remove(getUsername());
        }
        setRole(SessionAuthed.NOT_AUTHED);
    }

    public void write(Message message) {
        if(getChannel() != null) {
            getChannel().writeAndFlush(message);
        } else {
            System.out.println("[Socket Connect] Failed to write to channel channel = null, username =" +
                    (getUsername() != null ? getUsername() : "null (not authed)"));
        }
    }

    public void ping(int randomInt) {
        Message message = new Message();
        message.setId(String.valueOf(PingEvent.getId()));
        message.setChannel("Unknown");
        message.setMessage(String.valueOf(randomInt));
        message.setFromServer("Proxy");

        if(getChannel() != null) {
            getChannel().writeAndFlush(message);
        }
    }

    public enum SessionAuthed {
        NOT_AUTHED,
        AUTHED;
    }
}
