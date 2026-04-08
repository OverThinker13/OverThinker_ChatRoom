package com.example.java_chatroom.service;

import com.example.java_chatroom.entity.MessageSession;

import java.util.List;

public interface MessageSessionService {
    /** 获取指定用户的会话列表 */
    List<MessageSession> getSessionList(int userId);

    /** 创建新会话，返回 sessionId */
    int createSession(int userId, int toUserId);
}
