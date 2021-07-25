package net.shawshark.socketconnect.bukkit;

import lombok.Getter;
import net.shawshark.socketconnect.BridgeConnect;

import net.shawshark.socketconnect.INetwork;
import net.shawshark.socketconnect.bukkit.netty.EchoClient;
import net.shawshark.socketconnect.objects.Message;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SocketConnectPlugin extends JavaPlugin implements INetwork {

    @Getter private BridgeConnect bridgeConnect;

    @Getter private String serverUsername;
    @Getter private String ipAddress;
    @Getter private int port;

    @Getter private SocketConnectPlugin plugin;
    @Getter private EchoClient client;

    @Override
    public void onEnable() {
        bridgeConnect = new BridgeConnect(this);
        plugin = this;
        saveDefaultConfig();

        serverUsername = getConfig().getString("username", "Unknown");
        ipAddress = getConfig().getString("address");
        port = getConfig().getInt("port");

        new BukkitRunnable() {
            @Override
            public void run() {
                client = new EchoClient(getPlugin());
                client.startClient();
            }
        }.runTaskLater(this, 50);
    }

    @Override
    public void onDisable() {
        if(getClient() != null && getClient().isConnected()) {
            getClient().stopClient();
        }
    }

    @Override
    public void sendMessage(Message message) {

        if(message != null) {
            message.setFromServer(getServerUsername());

            if(getClient() != null) {
                getClient().sendMessage(message);
            }
        }
    }

    @Override
    public String getUsername() {
        return serverUsername;
    }
}