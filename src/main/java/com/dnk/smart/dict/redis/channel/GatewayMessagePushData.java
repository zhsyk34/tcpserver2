package com.dnk.smart.dict.redis.channel;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class GatewayMessagePushData {
    @NonNull
    private String sn;
    @NonNull
    private String message;
}
