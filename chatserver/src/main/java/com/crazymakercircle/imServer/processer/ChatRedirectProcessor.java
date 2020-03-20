package com.crazymakercircle.imServer.processer;

import com.crazymakercircle.im.common.bean.msg.ProtoMsg.HeadType;
import com.crazymakercircle.im.common.bean.msg.ProtoMsg.Message;
import com.crazymakercircle.imServer.server.ServerSession;
import com.crazymakercircle.imServer.server.SessionMap;
import com.crazymakercircle.util.Print;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.crazymakercircle.im.common.bean.msg.ProtoMsg.MessageRequest;

@Slf4j
@Service("ChatRedirectProcessor")
public class ChatRedirectProcessor extends AbstractServerProcessor {
    @Override
    public HeadType type() {
        return HeadType.MESSAGE_REQUEST;
    }

    @Override
    public boolean action(ServerSession fromSession, Message proto) {
        // 聊天处理
        MessageRequest msg = proto.getMessageRequest();
        Print.tcfo("chatMsg | from="
                + msg.getFrom()
                + " , to=" + msg.getTo()
                + " , content=" + msg.getContent());
        // 获取接收方的chatID
        String toId = msg.getTo();
        // int platform = msg.getPlatform();
        List<ServerSession> toSessions = SessionMap.instance().getSessionsBy(toId);
        if (toSessions == null) {
            //接收方离线
            Print.tcfo("[" + toId + "] 不在线，发送失败!");
        } else {
            toSessions.forEach((session) -> {
                // 将IM消息发送到接收方
                session.writeAndFlush(proto);
            });
        }
        return true;
    }

}
