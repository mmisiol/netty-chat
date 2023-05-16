package org.mmi.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMessageHistory implements MessageHistory {

    private final static int MAX_SIZE = 10;

    private final static Map<String, Object> MONITORS = new ConcurrentHashMap<>();

    private final Map<String, LinkedList<String>> channelLists = new ConcurrentHashMap<>();

    @Override
    public void put(String channel, String message) {
        Object monitor = MONITORS.computeIfAbsent(channel, c -> new Object());
        synchronized (monitor) {
            LinkedList<String> messages = channelLists.computeIfAbsent(channel, c -> new LinkedList<>());
            messages.addLast(message);
            while (messages.size() > MAX_SIZE) {
                messages.removeFirst();
            }
        }
    }

    @Override
    public List<String> latest(String channel, int length) {
        Object monitor = MONITORS.computeIfAbsent(channel, c -> new Object());
        synchronized (monitor) {
            List<String> history = channelLists.computeIfAbsent(channel, c -> new LinkedList<>());
            int from = Math.max(0, history.size() - length);
            int to = history.size();
            return history.subList(from, to);
        }
    }
}
