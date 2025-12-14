package com.example.java_chatroom.model;

// 使用这个类的对象来表示message_session_user表的一条记录
public class MessageSessionUserItem {
    private int sessionId;
    private int userId;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
