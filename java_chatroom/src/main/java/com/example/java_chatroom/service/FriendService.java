package com.example.java_chatroom.service;

import com.example.java_chatroom.entity.Friend;
import com.example.java_chatroom.entity.FriendRequest;

import java.util.List;

public interface FriendService {
    /** 获取好友列表 */
    List<Friend> getFriendList(int userId);

    /** 发送好友请求，返回提示信息 */
    String addFriend(int fromUserId, String fromUserName, int toUserId);

    /** 获取收到的好友请求列表 */
    List<FriendRequest> getFriendRequests(int userId);

    /** 处理好友请求，返回结果列表 */
    List<Object> handleRequest(int requestId, int status, int currentUserId);
}
