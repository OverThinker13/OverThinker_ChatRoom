package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.ApiResult;
import com.example.java_chatroom.entity.Friend;
import com.example.java_chatroom.entity.FriendRequest;
import com.example.java_chatroom.exception.BusinessException;
import com.example.java_chatroom.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
public class FriendController {

    @Resource
    private FriendService friendService;

    @GetMapping("/friendList")
    public ApiResult<List<Friend>> getFriendList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        List<Friend> friendList = friendService.getFriendList(userId);
        return ApiResult.success(friendList);
    }

    @PostMapping("/handleRequest")
    public ApiResult<List<Object>> handleRequest(@RequestBody FriendRequest request, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        List<Object> result = friendService.handleRequest(request.getRequestId(), request.getStatus(), userId);
        if (result.isEmpty()) {
            throw new BusinessException("请求不存在或无权限");
        }
        return ApiResult.success(result);
    }

    @GetMapping("/getFriendRequests")
    public ApiResult<List<FriendRequest>> getFriendRequests(HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        List<FriendRequest> requests = friendService.getFriendRequests(userId);
        return ApiResult.success(requests);
    }
}
