package org.mmi.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mmi.chat.Const.LINE_END;

@RequiredArgsConstructor
public class ChatHandler extends ChannelInboundHandlerAdapter {
    private static final String SERVER_PREFIX = "[SERVER]";
    private final PasswordHasher hasher;
    private final UsersRepository usersRepository;
    private final MessageHistory messageHistory;

    private final Map<String, Room> rooms;

    private Room room;
    private String name;


    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;

        switch (Command.parseCommand(message)) {
            case LOGIN -> handleLogin(ctx, message);
            case JOIN -> handleJoin(ctx, message);
            case LEAVE -> handleLeave(ctx);
            case LIST -> handleList(ctx);
            case USERS -> handleUsers(ctx);
            case HELP -> serverWrite(ctx, Command.help());
            case MESSAGE -> sendMessage(ctx, message);
            case DISCONNECT -> handleDisconnect(ctx);
            default -> serverWrite(ctx, "Unsupported operation");
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (room != null) {
            room.leave(ctx.channel());
        }
        room = null;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverWrite(ctx, "Welcome! Use /h for help!");
    }

    private void handleLogin(ChannelHandlerContext ctx, String message) {
        if (this.name != null) {
            serverWrite(ctx, "Already logged in as: " + this.name);
            return;
        }

        String[] words = message.split("\\s+");
        if (words.length < 3) {
            serverWrite(ctx, "/login command must be followed by <name> and <password>");
            return;
        }
        String name = words[1].trim();
        String password = words[2].trim();
        if (name.isBlank() || password.isBlank()) {
            serverWrite(ctx, "Name and password cannot be blank");
            return;
        }


        User user = usersRepository.findByName(name);
        if (user != null) {
            if (hasher.checkPassword(password, user.getPasswordHash())) {
                this.name = name;
                ctx.channel().attr(Const.NAME).set(name);
                serverWrite(ctx, "Logged in as user: " + name);
            } else {
                serverWrite(ctx, "Incorrect user or password");
            }
        } else {
            user = new User();
            user.setName(name);
            user.setPasswordHash(hasher.hashNewPassword(password));
            usersRepository.save(user);
            this.name = name;
            ctx.channel().attr(Const.NAME).set(name);
            serverWrite(ctx, "Created user: " + name);
        }

        if (user.getLastJoinedRoom() != null) {
            joinRoom(ctx, user.getLastJoinedRoom());
        }

    }

    private void handleJoin(ChannelHandlerContext ctx, String message) {
        if (needsLogin(ctx)) {
            return;
        }

        String[] words = message.split("\\s+");
        if (words.length < 2) {
            serverWrite(ctx, "/join command must be followed by <channel>");
            return;
        }
        String channelName = words[1].trim();
        joinRoom(ctx, channelName);
    }

    private void joinRoom(ChannelHandlerContext ctx, String roomName) {
        Room newRoom = rooms.computeIfAbsent(roomName, name -> new Room(roomName, messageHistory));
        if (newRoom.equals(this.room)) {
            serverWrite(ctx, "Already in channel: " + roomName);
            return;
        }

        if (!newRoom.join(ctx.channel())) {
            serverWrite(ctx, "Channel: " + roomName + " is full");
            return;
        }
        if (this.room != null) {
            this.room.leave(ctx.channel());
        }
        this.room = newRoom;

        User user = usersRepository.findByName(this.name);
        user.setLastJoinedRoom(roomName);
        usersRepository.save(user);

        StringBuilder output = new StringBuilder(SERVER_PREFIX + " joined room:" + roomName + LINE_END);

        List<String> messageHistory = this.messageHistory.latest(roomName, 5);

        messageHistory.forEach(output::append);
        ctx.channel().writeAndFlush(output.toString());
    }

    private void handleLeave(ChannelHandlerContext ctx) {
        if (needsLogin(ctx)) {
            return;
        }
        if (this.room == null) {
            serverWrite(ctx, "Not in a room");
            return;
        }

        Room room = this.room;
        this.room.leave(ctx.channel());
        this.room = null;
        serverWrite(ctx, "Left room:" + room.getName());
    }

    private void handleList(ChannelHandlerContext ctx) {
        if (needsLogin(ctx)) {
            return;
        }

        if (rooms.isEmpty()) {
            serverWrite(ctx, "No rooms available");
        } else {
            StringBuilder output = new StringBuilder("Available rooms:");
            output.append(LINE_END);
            output.append(rooms.keySet().stream().sorted().collect(Collectors.joining(LINE_END)));
            serverWrite(ctx, output.toString());
        }
    }

    private void handleUsers(ChannelHandlerContext ctx) {
        if (needsLogin(ctx)) {
            return;
        }
        if (this.room == null) {
            serverWrite(ctx, "you have to first join a channel using /join <channel> ");
            return;
        }

        StringBuilder output = new StringBuilder("Users in the room: " + room.getName() + LINE_END);
        output.append(this.room.listUsers().stream().collect(Collectors.joining(LINE_END)));

        serverWrite(ctx, output.toString());
    }

    private void handleDisconnect(ChannelHandlerContext ctx) {
        serverWrite(ctx, "bye bye");
        ctx.channel().close();
    }


    private void sendMessage(ChannelHandlerContext ctx, String message) {
        if (needsLogin(ctx)) {
            return;
        }

        if (this.room == null) {
            serverWrite(ctx, "you have to first join a channel using /join <channel> ");
            return;
        }

        String output = this.name + ":" + message + LINE_END;
        this.room.broadcast(ctx.channel(), output);
    }

    private boolean needsLogin(ChannelHandlerContext ctx) {
        if (this.name == null) {
            serverWrite(ctx, "login using /login first");
            return true;
        }
        return false;
    }

    private void serverWrite(ChannelHandlerContext ctx, String message) {
        ctx.channel().writeAndFlush(SERVER_PREFIX + " " + message + LINE_END);
    }
}