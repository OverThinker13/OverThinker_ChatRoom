package com.example.java_chatroom.mapper;

import com.example.java_chatroom.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendRequestMapper {
    // 发送好友请求
    int insertFriendRequest(FriendRequest request);

    // 根据用户ID查询收到的好友请求列表
    List<FriendRequest> selectRequestByUserId(int userId);

    // 根据请求ID查询请求详情
    FriendRequest selectRequestById(int requestId);

    // 同意好友请求
    int agreeRequest(int requestId);

    // 拒绝好友请求
    int rejectRequest(int requestId);
}