package org.mmi.chat;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Room {

    private final static int MAX_CLIENTS = 10;
    private final ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final Semaphore maxClients = new Semaphore(MAX_CLIENTS);

    @Getter
    private final String name;

    @Getter
    private final MessageHistory history;

    public boolean join(Channel client) {
        boolean joined = maxClients.tryAcquire();
        if (joined) {
            clients.add(client);
        }
        return joined;
    }

    public void leave(Channel client) {
        clients.remove(client);
        maxClients.release();
    }

    public void broadcast(Channel sender, String message) {
        history.put(name, message);
        String output = "[" + name + "]" + message;
        clients.writeAndFlush(output, channel -> channel != sender);
    }

    public List<String> listUsers() {
        return clients.stream().map(channel -> channel.attr(Const.NAME).get())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}

