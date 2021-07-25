package net.shawshark.socketconnect.request;

import net.shawshark.socketconnect.objects.Message;

public interface Listener {
    void execute(Message message);
    String getListenerName();
}