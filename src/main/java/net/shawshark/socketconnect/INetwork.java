package net.shawshark.socketconnect;

import net.shawshark.socketconnect.objects.Message;

public interface INetwork {
    void sendMessage(Message message);
    String getUsername();
}
