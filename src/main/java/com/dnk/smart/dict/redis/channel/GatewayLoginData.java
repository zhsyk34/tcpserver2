package com.dnk.smart.dict.redis.channel;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class GatewayLoginData {
    @NonNull
    private String sn;
    @NonNull
    private String serverId;//网关登录的服务器(其它服务器可以释放资源)
}
