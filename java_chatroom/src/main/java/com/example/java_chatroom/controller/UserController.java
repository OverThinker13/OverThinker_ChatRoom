package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.service.FriendService;
import com.example.java_chatroom.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Slf4j
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private FriendService friendService;

    @PostMapping("/login")
    public Object login(String username, String password, HttpServletRequest req) {
        User user = userService.login(username, password);
        if (user.getUserId() <= 0) {
            return new User();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        user.setPassword("");
        return user;
    }

    @PostMapping("/register")
    public Object register(String username, String password) {
        return userService.register(username, password);
    }

    @GetMapping("/userInfo")
    public Object getUserInfo(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new User();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new User();
        }
        user.setPassword("");
        return user;
    }

    @GetMapping("/searchUser")
    public Object searchUser(String username) {
        return userService.searchUser(username);
    }

    @PostMapping("/addFriend")
    public Object addFriend(@RequestParam int toUserId, HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return "请先登录";
        }
        User fromUser = (User) session.getAttribute("user");
        return friendService.addFriend(fromUser.getUserId(), fromUser.getUsername(), toUserId);
    }
}
