package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.ApiResult;
import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.exception.BusinessException;
import com.example.java_chatroom.service.FriendService;
import com.example.java_chatroom.service.RedisTokenService;
import com.example.java_chatroom.service.UserService;
import com.example.java_chatroom.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private FriendService friendService;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedisTokenService redisTokenService;

    @PostMapping("/login")
    public ApiResult<User> login(String username, String password, HttpServletResponse response) {
        User user = userService.login(username, password);
        if (user.getUserId() <= 0) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        redisTokenService.saveToken(user.getUserId(), token);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        user.setPassword("");
        return ApiResult.success(user);
    }

    @PostMapping("/register")
    public ApiResult<User> register(String username, String password) {
        User user = userService.register(username, password);
        if (user.getUserId() <= 0) {
            throw new BusinessException("用户名已存在");
        }
        return ApiResult.success("注册成功", user);
    }

    @GetMapping("/userInfo")
    public ApiResult<User> getUserInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        return ApiResult.success(user);
    }

    @GetMapping("/searchUser")
    public ApiResult<List<User>> searchUser(String username, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        List<User> users = userService.searchUser(username, userId);
        return ApiResult.success(users);
    }

    @PostMapping("/addFriend")
    public ApiResult<String> addFriend(@RequestParam int toUserId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        String result = friendService.addFriend(userId, username, toUserId);
        return ApiResult.success(result);
    }

    @GetMapping("/logout")
    public ApiResult<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId != null) {
            redisTokenService.removeToken(userId);
        }

        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ApiResult.success(null);
    }
}
