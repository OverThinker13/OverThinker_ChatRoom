package com.example.java_chatroom.mapper;

import com.example.java_chatroom.entity.Friend;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<Friend> selectFriendList(int userId);
}
