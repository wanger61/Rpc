package com.wyh.rpc.server;


import com.wyh.rpc.ApplicationContextUtil;
import com.wyh.rpc.RpcApplication;
import com.wyh.rpc.common.provider.RpcServiceProperties;
import com.wyh.rpc.common.remoting.transport.server.NettyServer;
import com.wyh.rpc.common.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.ui.context.support.UiApplicationContextUtils;

/**
 * Server: Automatic registration service via @RpcService annotation
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
//@RpcScan(basePackage = {"github.javaguide"})
@SpringBootApplication
public class NettyServerMain {

    public static void main(String[] args) {
        // Register service via annotation
        SpringApplication.run(RpcApplication.class, args);

        NettyServer bean = ApplicationContextUtil.getBean(NettyServer.class);
        bean.start();
    }
}
