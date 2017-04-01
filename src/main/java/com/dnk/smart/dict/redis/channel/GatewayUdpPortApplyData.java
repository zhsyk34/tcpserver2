package com.dnk.smart.dict.redis.channel;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class GatewayUdpPortApplyData {
    @NonNull
    private String ip;
    @NonNull
    private String sn;
    private int apply;
}
