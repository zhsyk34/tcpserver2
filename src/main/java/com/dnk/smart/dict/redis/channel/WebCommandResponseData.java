package com.dnk.smart.dict.redis.channel;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class WebCommandResponseData {
    @NonNull
    private String webServerId;
    @NonNull
    private String id;
    //TODO:是否需要改进为enum以详细描述失败原因?
    private boolean result;
}
