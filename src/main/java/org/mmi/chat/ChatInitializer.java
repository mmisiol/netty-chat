package org.mmi.chat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatInitializer extends ChannelInitializer<SocketChannel> {

    private final UsersRepository usersRepository = new InMemoryUserRepo();
    private final RoomHistory roomHistory = new RoomHistory();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new ChatHandler(new PasswordHasher(), usersRepository, roomHistory, rooms));
    }
}
