package com.example.java_chatroom.service.impl;

import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.mapper.UserMapper;
import com.example.java_chatroom.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByName(username);
        if (user == null || !user.getPassword().equals(password)) {
            log.warn("[login] 登录失败, username={}", username);
            return new User();
        }
        return user;
    }

    @Override
    public User register(String username, String password) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userMapper.insert(user);
            user.setPassword("");
            return user;
        } catch (DuplicateKeyException e) {
            log.warn("[register] 用户名已存在, username={}", username);
            return new User();
        }
    }

    @Override
    public User getUserById(int userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public List<User> searchUser(String username) {
        List<User> users = userMapper.selectByNameLike(username);
        for (User user : users) {
            user.setPassword("");
        }
        return users;
    }
}
