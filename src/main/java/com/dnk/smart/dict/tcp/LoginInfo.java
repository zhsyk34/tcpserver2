package com.dnk.smart.dict.tcp;

import com.alibaba.fastjson.JSONObject;
import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.Key;
import com.dnk.smart.dict.redis.cache.TcpSession;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

/**
 * 终端登录信息
 */
@NoArgsConstructor(staticName = "instance")
@Getter
@Setter
@Accessors(chain = true)
public final class LoginInfo {
    private String sn;
    private Device device;
    private int apply;
    private int allocated;
    private long happen;

    public static LoginInfo from(@NonNull JSONObject json) {
        String sn = json.getString(Key.SN.getName());
        Device device = Device.from(json.getIntValue(Key.TYPE.getName()));
        int apply = json.getIntValue(Key.APPLY.getName());
        return LoginInfo.instance().setSn(sn).setDevice(device).setApply(apply);
    }

    public LoginInfo update(@NonNull LoginInfo info) {
        if (StringUtils.hasText(info.sn)) {
            this.sn = info.sn;
        }
        if (info.device != null) {
            this.device = info.device;
        }
        if (info.apply >= Config.TCP_ALLOT_MIN_UDP_PORT) {
            this.apply = info.apply;
        }
        return this;
    }

    public TcpSession toTcpSession(@NonNull String ip, int port) {
        return TcpSession.of(Config.TCP_SERVER_ID, sn, ip, port, apply, allocated, happen);
    }

}