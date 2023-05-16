package org.mmi.chat;

import java.util.List;

public interface MessageHistory {
    void put(String channel, String message);

    List<String> latest(String channel, int length);
}
