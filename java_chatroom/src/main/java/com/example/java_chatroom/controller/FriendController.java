package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.FriendRequest;
import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class FriendController {

    @Resource
    private FriendService friendService;

    @GetMapping("/friendList")
    public Object getFriendList(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ArrayList<>();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ArrayList<>();
        }
        return friendService.getFriendList(user.getUserId());
    }

    @PostMapping("/handleRequest")
    public Object handleRequest(@RequestBody FriendRequest request, HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ArrayList<>();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ArrayList<>();
        }
        return friendService.handleRequest(request.getRequestId(), request.getStatus(), user.getUserId());
    }

    @GetMapping("/getFriendRequests")
    public Object getFriendRequests(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ArrayList<FriendRequest>();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ArrayList<FriendRequest>();
        }
        return friendService.getFriendRequests(user.getUserId());
    }
}
