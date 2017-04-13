package com.dnk.smart;

import com.dnk.smart.dict.Config;
import com.dnk.smart.logging.LoggerFactory;
import com.dnk.smart.tcp.server.TcpServer;
import com.dnk.smart.udp.UdpServer;
import com.dnk.smart.util.ThreadUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Entry {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");

        ExecutorService service = Executors.newCachedThreadPool();

        //1:udp server
        UdpServer udpServer = context.getBean(UdpServer.class);
        service.submit(udpServer::startup);
        while (UdpServer.getChannel() == null) {
            LoggerFactory.UDP_EVENT.logger("{} is starting...", UdpServer.class.getSimpleName());
            ThreadUtils.await(Config.SERVER_START_MONITOR_TIME);
        }

        //2:tcp server
        TcpServer tcpServer = context.getBean(TcpServer.class);
        service.submit(tcpServer::startup);
        while (!tcpServer.isStarted()) {
            LoggerFactory.TCP_EVENT.logger("{} is starting...", TcpServer.class.getSimpleName());
            ThreadUtils.await(Config.SERVER_START_MONITOR_TIME);
        }

        service.shutdown();

        //3:task
        //run in spring-config
//        TaskServer taskServer = context.getBean(TaskServer.class);
//        taskServer.startup();
    }

}
