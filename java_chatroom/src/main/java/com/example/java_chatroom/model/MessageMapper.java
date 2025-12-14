package com.example.java_chatroom.model;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    // 获取指定会话的最后一条消息
    String getLastMessageBySessionId(int sessionId);

    // 获取指定会话的历史消息
    // 有的会话里，历史消息可能特别特别多
    // 此处做出一个限制，默认只取最近的100条消息
    List<Message> getMessageBySessionId(int sessionId);

    // 通过这个方法实现插入消息到数据库表中
    void add(Message message);

}
