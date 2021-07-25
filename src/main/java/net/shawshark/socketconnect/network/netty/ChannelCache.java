package net.shawshark.socketconnect.network.netty;

import lombok.Getter;

import java.util.HashMap;

public class ChannelCache {
    @Getter private static HashMap<String, Session> getSessionByUsername = new HashMap<>();
}
