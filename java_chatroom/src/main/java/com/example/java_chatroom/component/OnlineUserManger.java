package com.example.java_chatroom.component;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;


// 通过这个类来记录当前用户的在线状态（维护了userId和WebSocketSession之间的映射）
@Component
public class OnlineUserManger {
    // 此处的哈希表要考虑到线程安全
    private ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 1) 用户上线，给这个哈希表中插入键值对
    public void  online(int userId, WebSocketSession session) {
        // 检查用户是否已经在线
        WebSocketSession oldSession = sessions.get(userId);
        if(oldSession != null) {
            // 如果用户已经在线，则关闭之前的连接
            try {
                oldSession.close();
                System.out.println("[" + userId + "] 之前的会话已关闭");
            } catch (Exception e) {
                System.out.println("[" + userId + "] 关闭之前会话时出现异常: " + e.getMessage());
            }
        }
        // 更新用户的会话
        sessions.put(userId, session);
        System.out.println("[" + userId + "] 上线");
    }

    // 2) 用户下线，针对哈希表进行删除元素
    public void offline(int userId, WebSocketSession session) {
        WebSocketSession existsSession = sessions.get(userId);
        if(existsSession == session) {
            // 如果是同一个session才真正进行下线操作
            sessions.remove(userId);
            System.out.println("[" + userId + "] 下线");
        }
    }

    // 3) 根据userId获取到WebSocketSession
    public WebSocketSession getSession(int userId) {
        return sessions.get(userId);
    }
}
