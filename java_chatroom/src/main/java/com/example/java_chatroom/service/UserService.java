package com.example.java_chatroom.service;

import com.example.java_chatroom.entity.User;
import java.util.List;

public interface UserService {
    User login(String username, String password);
    User register(String username, String password);
    User getUserById(int userId);
    List<User> searchUser(String username);
}
