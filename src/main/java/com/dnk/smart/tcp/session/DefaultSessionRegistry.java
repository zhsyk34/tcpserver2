package com.dnk.smart.tcp.session;

import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.redis.cache.Command;
import com.dnk.smart.dict.tcp.Device;
import com.dnk.smart.dict.tcp.LoginInfo;
import com.dnk.smart.dict.tcp.State;
import com.dnk.smart.logging.LoggerFactory;
import com.dnk.smart.tcp.cache.CacheAccessor;
import com.dnk.smart.tcp.command.CommandProcessor;
import com.dnk.smart.tcp.message.publish.ChannelMessageProcessor;
import com.dnk.smart.tcp.task.LoopTask;
import com.dnk.smart.util.TimeUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundInvoker;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.dnk.smart.dict.Config.*;
import static com.dnk.smart.dict.tcp.State.CLOSED;

@Service
public final class DefaultSessionRegistry implements SessionRegistry {

    private static final Map<String, Channel> ACCEPTS = new ConcurrentHashMap<>(Config.TCP_ACCEPT_COUNT_PREDICT);
    private static final Map<String, Channel> APPS = new ConcurrentHashMap<>(Config.TCP_APP_COUNT_PREDICT);
    private static final Map<String, Channel> GATEWAYS = new ConcurrentHashMap<>(Config.TCP_GATEWAY_COUNT_PREDICT);

    /**
     * 网关登录后在redisServer上登记
     * 网关下线后在redisServer上注销
     */
    @Resource
    private CacheAccessor cacheAccessor;

    /**
     * 网关登录成功后在tcpServer广播
     */
    @Resource
    private ChannelMessageProcessor channelMessageProcessor;

    /**
     * 网关登录成功后立即尝试执行任务
     * 网关关闭连接时尝试取消所有任务
     */
    @Resource
    private CommandProcessor commandProcessor;

    @Override
    public void registerOnActive(@NonNull Channel channel) {
        LoggerFactory.TCP_EVENT.logger("有新的连接");

        Channel original = ACCEPTS.put(cacheAccessor.id(channel), channel);
        if (original != null) {
            original.close();
        }
    }

    @Override
    public void registerAgainBeforeLogin(@NonNull Channel channel) {
        LoggerFactory.TCP_EVENT.logger("网关{}在登录前重新注册", cacheAccessor.info(channel).getSn());

        ACCEPTS.remove(cacheAccessor.id(channel));
        ACCEPTS.put(cacheAccessor.info(channel).getSn(), channel);
    }

    @Override
    public void registerAfterLogin(@NonNull Channel channel) {
        @NonNull
        LoginInfo info = cacheAccessor.info(channel);

        Channel original;
        switch (info.getDevice()) {
            case APP:
                String id = cacheAccessor.id(channel);

                if (!ACCEPTS.remove(id, channel)) {
                    return;
                }

                original = APPS.put(id, channel);

                LoggerFactory.TCP_EVENT.logger("app登录成功");
                break;
            case GATEWAY:
                String sn = info.getSn();

                if (!ACCEPTS.remove(sn, channel)) {
                    return;
                }

                LoggerFactory.TCP_EVENT.logger("网关[{}]登录成功", sn);

                original = GATEWAYS.put(sn, channel);

                cacheAccessor.registerTcpSession(info.toTcpSession(cacheAccessor.ip(channel), cacheAccessor.port(channel)));

                channelMessageProcessor.publishGatewayLogin(sn, TCP_SERVER_ID);

                commandProcessor.startup(channel);
                break;
            default:
                return;
        }

        if (original != null) {
            LoggerFactory.TCP_EVENT.logger("关闭超时连接[{}]", TimeUtils.format(TimeUtils.fromMillisecond(cacheAccessor.info(original).getHappen())));
            original.close();
        }
    }

    @Override
    public void unRegisterAfterClose(@NonNull Channel channel) {
        if (channel.isActive() || channel.isOpen()) {
            channel.close();//close confirm again for safe.
        }

        State state = cacheAccessor.state(channel);

        String id = cacheAccessor.id(channel);
        @NonNull
        LoginInfo info = cacheAccessor.info(channel);
        Device device = info.getDevice();
        String sn = info.getSn();

        if (state == null) {
            return;//未进行登记,直接关闭
        }

        switch (state) {
            case ACCEPT:
            case REQUEST:
            case VERIFY:
                //before allocate udp port,app and gateway has the same key to registry in the map
                if (ACCEPTS.remove(id, channel)) {
                    LoggerFactory.TCP_EVENT.logger("连接[{}]超时未登录已被注销", cacheAccessor.ip(channel));
                    return;
                }
                break;
            case AWAIT:
                //in this step gateway change the key and app will pass to next step quickly usually
                switch (device) {
                    case APP:
                        if (ACCEPTS.remove(id, channel)) {
                            LoggerFactory.TCP_EVENT.logger("app[{}]超时未登录已被注销", cacheAccessor.ip(channel));
                            return;
                        }
                        break;
                    case GATEWAY:
                        if (ACCEPTS.remove(sn, channel)) {
                            LoggerFactory.TCP_EVENT.logger("网关[{}]超时未登录已被注销", sn);
                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case SUCCESS:
                switch (device) {
                    case APP:
                        if (!APPS.remove(id, channel)) {
                            LoggerFactory.TCP_EVENT.logger("app[{}]关闭出错(可能因在线时长已到被移除)", cacheAccessor.ip(channel));
                        } else {
                            LoggerFactory.TCP_EVENT.logger("app[{}]注销连接", cacheAccessor.ip(channel));
                        }
                        break;
                    case GATEWAY:
                        if (GATEWAYS.remove(sn, channel)) {
                            cacheAccessor.unregisterTcpSession(sn);

                            cacheAccessor.state(channel, CLOSED);//close after success

                            commandProcessor.clean(channel);
                            LoggerFactory.TCP_EVENT.logger("网关[{}]下线,清空任务队列...", sn);
                        } else {
                            LoggerFactory.TCP_EVENT.logger(" 网关[{}]关闭出错(可能因在线时长已到被移除或重新登录时被关闭)", sn);
                        }
                        break;
                    default:
                        break;
                }
            case CLOSED:
            default:
                break;
        }
    }

    @Override
    public Channel getAcceptChannel(@NonNull String sn) {
        return ACCEPTS.get(sn);
    }

    @Override
    public Channel getGatewayChannel(@NonNull String sn) {
        return GATEWAYS.get(sn);
    }

    @Override
    public Channel getAppChannel(@NonNull String appId) {
        return APPS.get(appId);
    }

    @Override
    public void closeChannelQuietly(@NonNull String sn) {
        Optional.ofNullable(GATEWAYS.remove(sn)).ifPresent(ChannelOutboundInvoker::close);
    }

    @Override
    public List<LoopTask> monitor() {
        LoopTask countTask = () -> LoggerFactory.TCP_EVENT.logger("accept:[{}]\tapp:[{}]\t,gateway:[{}]", ACCEPTS.size(), APPS.size(), GATEWAYS.size());

        LoopTask acceptTask = () -> ACCEPTS.forEach((key, channel) -> {
            if (TimeUtils.timeout(cacheAccessor.info(channel).getHappen(), TCP_LOGIN_TIMEOUT)) {
                channel.close();
            }
        });

        LoopTask appTask = () -> APPS.forEach((id, channel) -> {
            if (TimeUtils.timeout(cacheAccessor.info(channel).getHappen(), TCP_APP_TIMEOUT)) {
                channel.close();
            }
        });

        LoopTask gatewayTask = () -> GATEWAYS.forEach((sn, channel) -> {
            if (TimeUtils.timeout(cacheAccessor.info(channel).getHappen(), TCP_GATEWAY_TIMEOUT)) {
                //call:channelInactive->close->onClose->unRegisterAfterClose
                channel.close();
            }
        });

        LoopTask commandTask = () -> GATEWAYS.forEach((sn, channel) -> {
            Command command = cacheAccessor.command(channel);
            if (command != null && TimeUtils.timeout(command.getHappen(), TCP_COMMAND_TIMEOUT)) {
                channel.close();
            }
        });

        return Collections.unmodifiableList(Arrays.asList(countTask, acceptTask, appTask, gatewayTask, commandTask));
    }

}
