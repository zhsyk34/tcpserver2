package com.dnk.smart.dict.redis.cache;

import lombok.*;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
@Accessors(chain = true)
public final class Command {
    private String id;//webServer中将其保存数据中的id
    @NonNull
    private String terminalId;
    @NonNull
    private String content;
    private long happen;//可选的(保存时间)
}