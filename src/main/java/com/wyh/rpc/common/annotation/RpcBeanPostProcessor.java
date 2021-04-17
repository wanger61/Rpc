package com.wyh.rpc.common.annotation;

import com.wyh.rpc.common.provider.RpcServiceProperties;
import com.wyh.rpc.common.provider.ServiceProvider;
import com.wyh.rpc.common.proxy.RpcClientProxy;
import com.wyh.rpc.common.remoting.transport.client.NettyClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
@Slf4j
public class RpcBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private ServiceProvider serviceProvider;

    @Autowired
    private NettyClient nettyClient;

    /**
     *  如果发现该bean被@RpcService标注则注册服务
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)){
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceProperties properties = RpcServiceProperties.builder()
                    .group(annotation.group())
                    .version(annotation.version())
                    .build();
            serviceProvider.publishService(bean, properties);
        }
        return bean;
    }

    /**
     *  如果发现某属性被@RpcReference标注则将为该属性赋值为动态代理对象
     */
    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field: declaredFields){
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if (annotation != null){
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                        .version(annotation.version())
                        .group(annotation.group()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient, rpcServiceProperties);
                Object proxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                field.set(bean, proxy);
            }
        }
        return bean;
    }
}
