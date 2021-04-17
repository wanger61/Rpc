package com.wyh.rpc.client;

import com.wyh.rpc.ApplicationContextUtil;
import com.wyh.rpc.RpcApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(RpcApplication.class, args);

        HelloController helloController = ApplicationContextUtil.getBean(HelloController.class);
        helloController.test();
    }
}
