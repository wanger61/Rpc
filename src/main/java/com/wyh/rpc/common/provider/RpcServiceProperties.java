package com.wyh.rpc.common.provider;

import lombok.*;

/**
 * 当需要使用版本号，标识具体实现类时，可以该Properties类构建
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcServiceProperties {

    private String group;

    private String version;

    private String serviceName;

    public String toRpcServiceName(){return this.getServiceName() + this.getGroup() + this.getVersion();}


}
