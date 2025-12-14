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
        if(sessions.get(userId) != null) {
            // 此时说明用户已经在线了，就登陆失败，不会记录这个映射关系，后续也就接受不到任何消息
            System.out.println("[" + userId + "] 已经被登陆了！");
            return;
        }
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
