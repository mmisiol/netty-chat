package org.mmi.chat;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

@RequiredArgsConstructor
public class Room {

    private final static int MAX_CLIENTS = 10;
    private final Set<Channel> clients = new HashSet<>();
    private final Semaphore maxClients = new Semaphore(MAX_CLIENTS);

    @Getter
    private final String name;

    @Getter
    private final RoomHistory history;

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
        for (Channel client : clients) {
            if (client != sender) {
                client.writeAndFlush("[" + name + "]" + message);
            }
        }
    }
}

