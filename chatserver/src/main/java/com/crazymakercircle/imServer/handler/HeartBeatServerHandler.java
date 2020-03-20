package com.crazymakercircle.imServer.handler;

import com.crazymakercircle.cocurrent.FutureTaskScheduler;
import com.crazymakercircle.imServer.server.ServerSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.crazymakercircle.im.common.bean.msg.ProtoMsg.HeadType;
import static com.crazymakercircle.im.common.bean.msg.ProtoMsg.Message;

@Slf4j
public class HeartBeatServerHandler extends IdleStateHandler {
    private static final int READ_IDLE_GAP = 150;

    public HeartBeatServerHandler() {
        super(READ_IDLE_GAP, 0, 0, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        //判断消息实例
        if (!(msg instanceof Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        Message pkg = (Message) msg;
        //判断消息类型
        HeadType headType = pkg.getType();
        if (headType.equals(HeadType.HEART_BEAT)) {
            //异步处理,将心跳包，直接回复给客户端
            FutureTaskScheduler.add(() -> {
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(msg);
                }
            });
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.info(READ_IDLE_GAP + "秒内未读到数据，关闭连接");
        ServerSession.closeSession(ctx);
    }
}