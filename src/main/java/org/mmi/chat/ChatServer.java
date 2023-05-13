package org.mmi.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatServer {
    private final int port;

    public static void main(String[] args) throws Exception {
        new ChatServer(8080).start();
    }


    public void start() throws InterruptedException {
        EventLoopGroup masterGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(masterGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChatInitializer());


        try {
            serverBootstrap.bind(port).sync().channel().closeFuture().sync();
        } finally {
            masterGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
