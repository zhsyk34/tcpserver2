package com.dnk.smart.dict.redis.channel;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public final class AppCommandResponseData {
    @NonNull
    private String appId;
    @NonNull
    //TODO:是否也以boolean直接反馈?
    private String result;
}
