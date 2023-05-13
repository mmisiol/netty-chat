package org.mmi.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatHandler extends ChannelInboundHandlerAdapter {
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final AttributeKey<String> USER_NAME = AttributeKey.valueOf("userName");
    private static final String SERVER_PREFIX = "[SERVER]";

    private final PasswordHasher hasher;
    private final UsersRepository usersRepository;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;

        switch (Command.parseCommand(message)) {
            case LOGIN -> handleLogin(ctx, message);
            default -> serverWrite(ctx, "Unsupported operation");
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channels.add(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channels.remove(channel);
    }

    private void handleLogin(ChannelHandlerContext ctx, String message) {
        if (ctx.channel().hasAttr(USER_NAME)) {
            String name = ctx.channel().attr(USER_NAME).get();
            serverWrite(ctx, "Already logged in as: " + name);
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
        SpinLock lock = SpinLock.forString(name);
        lock.acquire();
        try {
            User user = usersRepository.findByName(name);
            if (user != null) {
                if (hasher.checkPassword(password, user.getPasswordHash())) {
                    ctx.channel().attr(USER_NAME).set(name);
                    serverWrite(ctx, "Logged in as user: " + name);
                } else {
                    serverWrite(ctx, "Incorrect user or password");
                }
            } else {
                user = new User();
                user.setName(name);
                user.setPasswordHash(hasher.hashNewPassword(password));
                usersRepository.save(user);
                ctx.channel().attr(USER_NAME).set(name);
                serverWrite(ctx, "Created user: " + name);
            }
        } finally {
            lock.release();
        }
    }

    private void serverWrite(ChannelHandlerContext ctx, String message) {
        ctx.channel().writeAndFlush(SERVER_PREFIX + " " + message + "\n\r");
    }
}