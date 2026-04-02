package com.example.java_chatroom.mapper;

import com.example.java_chatroom.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    // 把用户插入到数据库中 -> 注册
    int insert(User user);

    // 根据用户名查询用户信息 -> 登录
    User selectByName(String username);

    // 根据用户ID查询用户信息
    User selectById(int userId);

    // 根据用户名模糊查询用户列表 -> 搜索好友
    List<User> selectByNameLike(@Param("username") String username);
}