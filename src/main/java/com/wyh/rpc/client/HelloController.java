package com.wyh.rpc.client;

import com.wyh.rpc.common.annotation.RpcReference;
import com.wyh.rpc.common.service.Hello;
import com.wyh.rpc.common.service.HelloService;
import org.springframework.stereotype.Component;

/**
 * @author smile2coder
 */
@Component
public class HelloController {

    @RpcReference
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("111", "222xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        //assert "Hello description is 222".equals(hello);
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")));
        }
    }
}
