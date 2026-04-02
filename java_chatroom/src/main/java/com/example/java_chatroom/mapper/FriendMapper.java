package com.example.java_chatroom.mapper;

import com.example.java_chatroom.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendMapper {
    // 查询好友列表
    List<Friend> selectFriendList(int userId);

    // 添加好友关系
    int insertFriend(Friend friend);

    // 检查是否已经是好友
    int countByUserIdAndFriendId(@Param("userId") int userId, @Param("friendId") int friendId);
}