package com.wyh.rpc.common.annotation;

import java.lang.annotation.*;

/**
 * 该注解用于服务调用，被该注解标注的类会通过Rpc方式调用远程服务
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    String version() default "";

    String group() default "";

}
