package com.wyh.rpc.common.annotation;

import java.lang.annotation.*;

/**
 * 该注解用于服务注册，标注在服务的实现类上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    String version() default "";

    String group() default "";

}
