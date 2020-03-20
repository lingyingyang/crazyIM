package com.crazymakercircle.imServer.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.crazymakercircle.imServer")
@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        // 启动并初始化 Spring 环境及其各 Spring 组件
        ApplicationContext context = SpringApplication.run(ServerApplication.class, args);
        ChatServer nettyServer = context.getBean(ChatServer.class);
        nettyServer.run();
    }
}
