package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.Friend;
import com.example.java_chatroom.entity.FriendRequest;
import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.mapper.FriendMapper;
import com.example.java_chatroom.mapper.FriendRequestMapper;
import com.example.java_chatroom.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class FriendController {

    @Resource
    private FriendMapper friendMapper;

    @Resource
    private FriendRequestMapper friendRequestMapper;

    @Resource
    private UserMapper userMapper;

    @GetMapping("/friendList")
    public Object getFriendList(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ArrayList<Friend>();
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ArrayList<Friend>();
        }

        List<Friend> friendList = friendMapper.selectFriendList(user.getUserId());

        // 遍历好友列表，查询每个好友的详细信息
        for (Friend friend : friendList) {
            User friendUser = userMapper.selectById(friend.getFriendId());
            if (friendUser != null) {
                friend.setFriendName(friendUser.getUsername());
            }
        }

        return friendList;
    }

    // 处理好友请求（同意/拒绝）
    @PostMapping("/handleRequest")
    public Object handleRequest(@RequestBody FriendRequest request, HttpServletRequest req) {
        // 关键修复：强制获取Session，即使是@RequestBody也能正常读取
        HttpSession session = req.getSession(true); // 用true强制创建，避免null
        if (session == null) {
            return new ArrayList<>();
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ArrayList<>();
        }

        // 原有逻辑不变
        FriendRequest dbRequest = friendRequestMapper.selectRequestById(request.getRequestId());
        if (dbRequest == null || dbRequest.getToUserId() != user.getUserId()) {
            return new ArrayList<>();
        }

        if (request.getStatus() == 1) {
            friendRequestMapper.agreeRequest(request.getRequestId());
            // 双向添加好友逻辑不变
            Friend friend1 = new Friend();
            friend1.setFriendId(dbRequest.getToUserId());
            friend1.setUserId(dbRequest.getFromUserId());
            friend1.setStatus(1);
            friend1.setCreateTime(new Date());
            friendMapper.insertFriend(friend1);

            Friend friend2 = new Friend();
            friend2.setFriendId(dbRequest.getFromUserId());
            friend2.setUserId(dbRequest.getToUserId());
            friend2.setStatus(1);
            friend2.setCreateTime(new Date());
            friendMapper.insertFriend(friend2);

            // 返回更新后的双方好友列表
            List<Friend> friendList1 = friendMapper.selectFriendList(dbRequest.getFromUserId());
            for (Friend f : friendList1) {
                User friendUser = userMapper.selectById(f.getFriendId());
                if (friendUser != null) {
                    f.setFriendName(friendUser.getUsername());
                }
            }

            List<Friend> friendList2 = friendMapper.selectFriendList(dbRequest.getToUserId());
            for (Friend f : friendList2) {
                User friendUser = userMapper.selectById(f.getFriendId());
                if (friendUser != null) {
                    f.setFriendName(friendUser.getUsername());
                }
            }

            List<Object> result = new ArrayList<>();
            result.add("已添加为好友");
            result.add(friendList1);
            result.add(friendList2);
            return result;
        } else {
            friendRequestMapper.rejectRequest(request.getRequestId());
            return new ArrayList<>();
        }
    }

    // 获取好友请求列表
    @GetMapping("/getFriendRequests")
    public Object getFriendRequests(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ArrayList<FriendRequest>();
        }

        User user = (User) session.getAttribute("user");
        List<FriendRequest> requests = friendRequestMapper.selectRequestByUserId(user.getUserId());
        return requests;
    }
}
