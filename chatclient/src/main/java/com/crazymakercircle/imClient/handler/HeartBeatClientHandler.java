package com.crazymakercircle.imClient.handler;


import com.crazymakercircle.im.common.bean.User;
import com.crazymakercircle.imClient.client.ClientSession;
import com.crazymakercircle.imClient.protoBuilder.HeartBeatMsgBuilder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.crazymakercircle.im.common.bean.msg.ProtoMsg.HeadType;
import static com.crazymakercircle.im.common.bean.msg.ProtoMsg.Message;

@Slf4j
@ChannelHandler.Sharable
@Service("HeartBeatClientHandler")
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    //心跳的时间间隔，单位为s
    private static final int HEARTBEAT_INTERVAL = 100;

    //在Handler被加入到Pipeline时，开始发送心跳
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ClientSession session = ClientSession.getSession(ctx);
        User user = session.getUser();
        HeartBeatMsgBuilder builder = new HeartBeatMsgBuilder(user, session);

        Message message = builder.buildMsg();
        //发送心跳
        heartBeat(ctx, message);
    }

    //使用定时器，发送心跳报文
    public void heartBeat(ChannelHandlerContext ctx, Message heartbeatMsg) {
        ctx.executor().schedule(() -> {
            if (ctx.channel().isActive()) {
                log.info(" 发送 HEART_BEAT  消息 to server");
                ctx.writeAndFlush(heartbeatMsg);
                //递归调用，发送下一次的心跳
                heartBeat(ctx, heartbeatMsg);
            }

        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 接受到服务器的心跳回写
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (!(msg instanceof Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        //判断类型
        Message pkg = (Message) msg;
        HeadType headType = pkg.getType();
        if (headType.equals(HeadType.HEART_BEAT)) {
            log.info(" 收到回写的 HEART_BEAT  消息 from server");
            return;
        }

        super.channelRead(ctx, msg);
    }
}
