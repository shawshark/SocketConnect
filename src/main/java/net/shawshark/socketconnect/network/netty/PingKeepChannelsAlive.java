package net.shawshark.socketconnect.network.netty;

import lombok.Getter;

import java.util.Random;

public class PingKeepChannelsAlive implements Runnable {

    @Getter private Thread thread;

    public PingKeepChannelsAlive() {
        if(getThread() != null) return;

        thread = new Thread(this);
        thread.setName("Ping Keep Alive Thread");
        thread.start();
    }

    @Override
    public void run() {
        try {
            Random random = new Random();
            while(getThread() != null) {
                for(Session session : ChannelCache.getGetSessionByUsername().values()) {
                    session.ping(random.nextInt());
                }
                Thread.sleep(10000);
            }
        } catch (Exception e) {}
    }
}
