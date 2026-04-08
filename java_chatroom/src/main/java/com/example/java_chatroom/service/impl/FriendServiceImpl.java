package com.example.java_chatroom.service.impl;

import com.example.java_chatroom.entity.Friend;
import com.example.java_chatroom.entity.FriendRequest;
import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.mapper.FriendMapper;
import com.example.java_chatroom.mapper.FriendRequestMapper;
import com.example.java_chatroom.mapper.UserMapper;
import com.example.java_chatroom.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FriendServiceImpl implements FriendService {

    @Resource
    private FriendMapper friendMapper;

    @Resource
    private FriendRequestMapper friendRequestMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<Friend> getFriendList(int userId) {
        List<Friend> friendList = friendMapper.selectFriendList(userId);
        for (Friend friend : friendList) {
            User friendUser = userMapper.selectById(friend.getFriendId());
            if (friendUser != null) {
                friend.setFriendName(friendUser.getUsername());
            }
        }
        return friendList;
    }

    @Override
    public String addFriend(int fromUserId, String fromUserName, int toUserId) {
        int isFriend = friendMapper.countByUserIdAndFriendId(fromUserId, toUserId);
        if (isFriend > 0) {
            return "已经是好友了";
        }
        List<FriendRequest> existingRequests = friendRequestMapper.selectRequestByUserId(toUserId);
        for (FriendRequest request : existingRequests) {
            if (request.getFromUserId() == fromUserId && request.getStatus() == 0) {
                return "请求已发送，等待对方确认";
            }
        }
        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setFromUserName(fromUserName);
        request.setToUserId(toUserId);
        int ret = friendRequestMapper.insertFriendRequest(request);
        return ret > 0 ? "请求已发送，等待对方确认" : "发送失败";
    }

    @Override
    public List<FriendRequest> getFriendRequests(int userId) {
        return friendRequestMapper.selectRequestByUserId(userId);
    }

    @Override
    public List<Object> handleRequest(int requestId, int status, int currentUserId) {
        List<Object> result = new ArrayList<>();
        FriendRequest dbRequest = friendRequestMapper.selectRequestById(requestId);
        if (dbRequest == null || dbRequest.getToUserId() != currentUserId) {
            return result;
        }
        if (status == 1) {
            friendRequestMapper.agreeRequest(requestId);

            Friend friend1 = new Friend();
            friend1.setUserId(dbRequest.getFromUserId());
            friend1.setFriendId(dbRequest.getToUserId());
            friend1.setStatus(1);
            friend1.setCreateTime(new Date());
            friendMapper.insertFriend(friend1);

            Friend friend2 = new Friend();
            friend2.setUserId(dbRequest.getToUserId());
            friend2.setFriendId(dbRequest.getFromUserId());
            friend2.setStatus(1);
            friend2.setCreateTime(new Date());
            friendMapper.insertFriend(friend2);

            result.add("已添加为好友");
            result.add(getFriendList(dbRequest.getFromUserId()));
            result.add(getFriendList(dbRequest.getToUserId()));
        } else {
            friendRequestMapper.rejectRequest(requestId);
        }
        return result;
    }
}
