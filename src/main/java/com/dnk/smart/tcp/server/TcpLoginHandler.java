package com.dnk.smart.tcp.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dnk.smart.dict.Action;
import com.dnk.smart.dict.Key;
import com.dnk.smart.dict.Result;
import com.dnk.smart.dict.tcp.LoginInfo;
import com.dnk.smart.tcp.cache.CacheAccessor;
import com.dnk.smart.tcp.state.StateController;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.dnk.smart.dict.tcp.State.SUCCESS;

@Component
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ChannelHandler.Sharable
final class TcpLoginHandler extends ChannelInboundHandlerAdapter {
    @Resource
    private StateController stateController;
    @Resource
    private CacheAccessor dataAccessor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof String)) {
            return;
        }
        String command = (String) msg;
//        Log.logger(Factory.TCP_RECEIVE, command);

        Channel channel = ctx.channel();

        JSONObject json = JSON.parseObject(command);

        //1.login request
        Action action = Action.from(json.getString(Key.ACTION.getName()));

        if (action == Action.LOGIN_REQUEST) {
            stateController.request(channel, LoginInfo.from(json));
            return;
        }

        //2.verify and allocate port(gateway)
        Result result = Result.from(json.getString(Key.RESULT.getName()));
        String keyCode = json.getString(Key.KEYCODE.getName());
        if (result == Result.OK && keyCode != null) {
            stateController.verify(channel, keyCode);
            return;
        }

        //3.filter un-login client
        if (dataAccessor.state(channel) == SUCCESS) {
            ctx.fireChannelRead(command);
        } else {
            stateController.close(channel);
        }
    }

}
