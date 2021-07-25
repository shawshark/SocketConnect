package net.shawshark.socketconnect;

import lombok.Getter;
import net.shawshark.socketconnect.objects.Message;
import net.shawshark.socketconnect.objects.MessageEvent;
import net.shawshark.socketconnect.request.Listener;

import java.util.ArrayList;
import java.util.List;

public class BridgeConnect {

    @Getter private List<Listener> listeners;
    @Getter private static BridgeConnect connect;
    @Getter private INetwork network;

    public BridgeConnect(INetwork network) {
        listeners = new ArrayList<>();

        //API
        connect = this;
        this.network = network;
    }

    public void registerListener(Listener listener) {
        if(getListeners().contains(listener)) {
            System.out.println("[Socket Connect] Failed to register listener, Listener already registered!");
            return;
        }

        getListeners().add(listener);
        System.out.println("[Socket Connect] Registered listener name: " + listener.getListenerName());
    }

    public void sendMessage(Message message) {
        getNetwork().sendMessage(message);
    }
}