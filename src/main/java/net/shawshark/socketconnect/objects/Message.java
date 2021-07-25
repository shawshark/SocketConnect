package net.shawshark.socketconnect.objects;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {

    @Getter private static final Gson gson = new Gson();

    @SerializedName("id")
    @Getter@Setter private String id;

    // set this from an INetwork interface
    @SerializedName("from")
    @Getter@Setter private String fromServer;

    @SerializedName("ch")
    @Getter@Setter private String channel;

    @SerializedName("msg")
    @Getter@Setter private String message;

    @SerializedName("to")
    @Getter@Setter private String toServers;

    public Message() {}

    public Message(String id, String channel, String message, String toServers) {
        this.id = id;
        this.channel = channel;
        this.message = message;

        this.toServers = toServers;
    }

    public static Message decode(String string) { return gson.fromJson(string, Message.class);}
    public static String encode(Message message) {
        return gson.toJson(message);
    }

    public boolean containsUsername(String username) {
        if(getToServers().equalsIgnoreCase("")){
            return false;
        }

        for(String rep : getToServers().split(",")) {
            if(rep.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
}