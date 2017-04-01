package com.dnk.smart.udp;

import com.dnk.smart.dict.Config;
import com.dnk.smart.logging.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * record udp session toUdpSession awake gateway
 */
@Service
public final class UdpServer {

    @Getter
    private static volatile Channel channel;

    @Resource
    private UdpCoder udpCoder;
    @Resource
    private UdpHandler udpHandler;

    public void startup() {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group).channel(NioDatagramChannel.class);
            bootstrap.option(ChannelOption.SO_BROADCAST, false);
            bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
                @Override
                protected void initChannel(DatagramChannel ch) throws Exception {
                    ch.pipeline().addLast(udpCoder, udpHandler);
                }
            });

            channel = bootstrap.bind(Config.UDP_SERVER_PORT).syncUninterruptibly().channel();

            LoggerFactory.UDP_EVENT.logger("{} startup success at port[{}]", this.getClass().getSimpleName(), Config.UDP_SERVER_PORT);
            channel.closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
