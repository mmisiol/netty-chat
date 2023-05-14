package org.mmi.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomHistory {

    private final static int SIZE = 5;

    private final static Map<String, Object> MONITORS = new ConcurrentHashMap<>();

    private final Map<String, LinkedList<String>> channelLists = new ConcurrentHashMap<>();

    public void put(String channel, String message) {
        Object monitor = MONITORS.computeIfAbsent(channel, c -> new Object());
        synchronized (monitor) {
            LinkedList<String> messages = channelLists.computeIfAbsent(channel, c -> new LinkedList<>());
            messages.addLast(message);
            while (messages.size() > SIZE) {
                messages.removeFirst();
            }
        }
    }

    public List<String> getMessageHistory(String channel) {
        Object monitor = MONITORS.computeIfAbsent(channel, c -> new Object());
        synchronized (monitor) {
            return new LinkedList<>(channelLists.computeIfAbsent(channel, c -> new LinkedList<>()));
        }
    }
}
