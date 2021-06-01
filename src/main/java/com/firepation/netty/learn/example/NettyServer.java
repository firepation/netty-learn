package com.firepation.netty.learn.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyServer {

    public static void main(String[] args) {

        // 监听端口, accpet 新连接的线程组
        NioEventLoopGroup boss = new NioEventLoopGroup();
        // 处理每一条连接的数据读写的线程组
        NioEventLoopGroup worker = new NioEventLoopGroup();
        // 负责服务端的启动工作
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
            // 添加服务启动逻辑
            .handler(new ChannelInitializer<NioServerSocketChannel>() {
                protected void initChannel(NioServerSocketChannel ch) {
                    System.out.println("服务端启动钟");
                }
            })
            // 给服务端的channel添加一些自定义属性
            .attr(AttributeKey.newInstance("serverName"), "nettyServer")
            // 指定工作线程组
            .group(boss, worker)
            // 指定IO模型
            .channel(NioServerSocketChannel.class)
            // 设置数据处理逻辑
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                            System.out.println(msg);
                        }
                    });
                }
            })
            // 给每条连接设置一些TCP底层相关的属性
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            // 给服务端channel设置一些属性
            .option(ChannelOption.SO_BACKLOG, 1024);
        bind(serverBootstrap, 1000);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) {
                if (future.isSuccess()) {
                    System.out.println("端口[" + port + "]绑定成功!");
                } else {
                    System.err.println("端口[" + port + "]绑定失败");
                    bind(serverBootstrap, port + 1);
                }
            }
        });
    }
}
