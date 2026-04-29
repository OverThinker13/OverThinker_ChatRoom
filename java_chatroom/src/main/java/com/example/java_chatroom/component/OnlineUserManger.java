package com.example.java_chatroom.component;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserManger {

    private static final String ONLINE_KEY = "online_users";

    private final ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;

    public OnlineUserManger(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void online(int userId, WebSocketSession session) {
        WebSocketSession oldSession = sessions.get(userId);
        if (oldSession != null) {
            try {
                oldSession.close();
                System.out.println("[" + userId + "] 之前的会话已关闭");
            } catch (Exception e) {
                System.out.println("[" + userId + "] 关闭之前会话时出现异常: " + e.getMessage());
            }
        }
        sessions.put(userId, session);
        redisTemplate.opsForSet().add(ONLINE_KEY, String.valueOf(userId));
        System.out.println("[" + userId + "] 上线");
    }

    public void offline(int userId, WebSocketSession session) {
        WebSocketSession existsSession = sessions.get(userId);
        if (existsSession == session) {
            sessions.remove(userId);
            redisTemplate.opsForSet().remove(ONLINE_KEY, String.valueOf(userId));
            System.out.println("[" + userId + "] 下线");
        }
    }

    public WebSocketSession getSession(int userId) {
        return sessions.get(userId);
    }

    public boolean isOnline(int userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_KEY, String.valueOf(userId)));
    }
}
