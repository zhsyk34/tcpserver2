package com.dnk.smart.dict.redis.channel;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class WebCommandRequestData {
    @NonNull
    private String serverId;//受理服务器(唤醒网关)
    @NonNull
    private String sn;//目标网关
}
