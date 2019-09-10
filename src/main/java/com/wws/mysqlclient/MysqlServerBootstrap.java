package com.wws.mysqlclient;

import com.wws.mysqlclient.handler.RecieveHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author wws
 * @version 1.0.0
 * @date 2019-09-06 17:39
 **/
public class MysqlServerBootstrap {

    public static void main(String[] args) {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .localAddress(3307)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RecieveHandler());
                    }
                });
        serverBootstrap.bind().addListener((future -> {
            if(future.isSuccess()){
                System.out.println("端口绑定成功");
            }else{
                System.out.println("端口绑定失败:"+future.cause());
            }
        }));
    }

}
