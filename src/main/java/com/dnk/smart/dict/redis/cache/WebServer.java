package com.dnk.smart.dict.redis.cache;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class WebServer {
    @NonNull
    private String serverId;
    private long happen;
}
