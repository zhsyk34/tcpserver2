package com.dnk.smart.dict.redis.channel;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class AppCommandRequestData {
    @NonNull
    private String sn;//目标网关(必要时先唤醒)
}
