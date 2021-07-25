package net.shawshark.socketconnect.network;

import lombok.Getter;
import lombok.Setter;
import net.shawshark.socketconnect.BridgeConnect;
import net.shawshark.socketconnect.INetwork;
import net.shawshark.socketconnect.network.netty.EchoServer;
import net.shawshark.socketconnect.objects.Message;
import net.shawshark.socketconnect.objects.MessageEvent;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

import java.io.*;
import java.util.Scanner;

public class SocketConnect implements INetwork {

    @Getter static private SocketConnect instance;
    @Getter private BridgeConnect bridgeConnect;

    @Getter@Setter private File dataFolder;
    @Getter@Setter private Config serviceConfig;
    @Getter@Setter private FileConfiguration config;

    @Getter private EchoServer server;

    @Getter private String username;
    @Getter private String ipAddress;
    @Getter private int port;

    public SocketConnect() {
        instance = this;

        // Set folder that the jar file was started up in
        setDataFolder(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile());
        // Load config
        loadConfig();

        //setup bridge
        bridgeConnect = new BridgeConnect(this);

        //setup server
        server = new EchoServer(this);
    }

    public static void main(String[] args) {

        new SocketConnect();
        getInstance().log("Starting up.. For help type 'help'");
        Scanner scanner = new Scanner(System.in);

        try {
            while(true) {
                String input = scanner.nextLine();
                if(input.equalsIgnoreCase("stop") || input.equalsIgnoreCase("exit") ||
                        input.equalsIgnoreCase("disable")) {

                    getInstance().log("Stopping Socket Connect...");
                    if(getInstance().getServer() != null && getInstance().getServer().isConnected()) {
                        getInstance().getServer().disconnect();
                    }
                    System.exit(0);

                } else if(input.equalsIgnoreCase("help")) {
                    getInstance().sendHelpToConsole();
                } else if(input.equalsIgnoreCase("disconnect")) {

                    if(getInstance().getServer() != null) {
                        if(getInstance().getServer().isConnected()) {
                            getInstance().getServer().disconnect();
                            getInstance().log("Disconnected server instance!");
                        } else {
                            getInstance().log("Server isn't active, therefor we can't disconnect");
                        }
                    } else {
                        getInstance().log("Server isn't active, therefor we can't disconnect");
                    }
                } else if(input.equalsIgnoreCase("test")) {
                    test();
                } else {
                    getInstance().log("Unknown command '" + input + "'. Type 'help' for list of commands");
                }
            }
        } catch(Exception e) {}
    }


    public static void test() {
        getInstance().sendMessage(new Message(String.valueOf(MessageEvent.getId()),
                "main", "Channel_name", "this is a test message"));
    }

    public void loadConfig() {

        File file = new File(getDataFolder(), "config.yml");
        boolean exists = (file.exists());

        serviceConfig = new Config(this, getDataFolder(), "config.yml");
        config = getServiceConfig().getConfig();

        if(!exists) {
            // Set defaults
            getConfig().set("username", "Main-Server");
            getConfig().set("address", "127.0.0.1");
            getConfig().set("port", 30000);

            getServiceConfig().saveConfig();
        }

        username = getConfig().getString("username");
        ipAddress = getConfig().getString("address");
        port = getConfig().getInt("port");
    }

    public void sendHelpToConsole() {
        log("help - Shows help list");
        log("enable - Enable socket connect");
        log("disable - Disable socket connect");
        log("disconnect - Disconnect netty server (turn it off)");
        log("[stop,exit] - Stop scoket connect and close program");
    }

    public void log(String message) {
        System.out.println("[Socket Connect] " + message);
    }

    @Override
    public void sendMessage(Message message) {

        if(message != null) {
            message.setFromServer("Main Server");
        }

        if(getServer() != null) {
            //etServer().writingToChannel(Message.encode(message));
            getServer().sendMessage(message);
        }
    }

    @Override
    public String getUsername() {
        return "Proxy #1";
    }
}
