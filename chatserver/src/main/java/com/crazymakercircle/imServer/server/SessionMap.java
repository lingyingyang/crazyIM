package com.crazymakercircle.imServer.server;

import com.crazymakercircle.im.common.bean.User;
import com.crazymakercircle.util.Print;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Data
public final class SessionMap {
    private SessionMap() {
    }

    private static SessionMap singleInstance = new SessionMap();

    //会话集合
    private ConcurrentHashMap<String, ServerSession> map = new ConcurrentHashMap<>();

    public static SessionMap instance() {
        return singleInstance;
    }

    /**
     * 增加session对象
     */
    public void addSession(
            String sessionId, ServerSession s) {
        map.put(sessionId, s);
        log.info("用户登录:id= " + s.getUser().getUid()
                + "   在线总数: " + map.size());
    }

    /**
     * 获取session对象
     */
    public ServerSession getSession(String sessionId) {
        return map.getOrDefault(sessionId, null);
    }

    /**
     * 根据用户id，获取session对象
     */
    public List<ServerSession> getSessionsBy(String userId) {
        return map.values()
                  .stream()
                  .filter(s -> s.getUser().getUid().equals(userId))
                  .collect(Collectors.toList());
    }

    /**
     * 删除session
     */
    public void removeSession(String sessionId) {
        if (!map.containsKey(sessionId)) {
            return;
        }
        ServerSession s = map.get(sessionId);
        map.remove(sessionId);
        Print.tcfo("用户下线:id= " + s.getUser().getUid()
                + "   在线总数: " + map.size());
    }


    public boolean hasLogin(User user) {
        for (Entry<String, ServerSession> entry : map.entrySet()) {
            User u = entry.getValue().getUser();
            if (u.getUid().equals(user.getUid())
                    && u.getPlatform().equals(user.getPlatform())) {
                return true;
            }
        }
        return false;
    }
}
