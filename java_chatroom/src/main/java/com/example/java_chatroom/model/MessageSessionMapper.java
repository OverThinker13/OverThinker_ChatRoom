package com.example.java_chatroom.model;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageSessionMapper {
    // 1.根据userId获取到该用户都在哪些会话中存在，返回结果是一组sessionId
    List<Integer> getSessionIdByUserId(int userId);

    // 2.根据sessionId再来查询这个会话都包含了哪些用户，（刨除最初的自己）
    List<Friend> getFriendsIdBySessionId(int sessionId, int selfUserId);

    // 3.新增一个会话记录。返回会话id
    // 这样的方法返回值int表示的时这样的插入操作影响到几行，此处获取的sessionId是通过参数的session属性获取的
    int addMessageSession(MessageSession messageSession);

    // 4.给message_session_user表也新增对应的纪录
    void addMessageSessionUser(MessageSessionUserItem messageSessionUserItem);
}
