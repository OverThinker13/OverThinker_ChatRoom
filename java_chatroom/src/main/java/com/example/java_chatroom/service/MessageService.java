package com.example.java_chatroom.service;

import com.example.java_chatroom.entity.Message;

import java.util.List;

public interface MessageService {
    /** 获取指定会话的历史消息（最近100条，升序） */
    List<Message> getMessageList(int sessionId);
}
