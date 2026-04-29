package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.ApiResult;
import com.example.java_chatroom.entity.MessageSession;
import com.example.java_chatroom.exception.BusinessException;
import com.example.java_chatroom.service.MessageSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
public class MessageSessionController {

    @Resource
    private MessageSessionService messageSessionService;

    @GetMapping("/sessionList")
    public ApiResult<List<MessageSession>> getMessageSessionList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        List<MessageSession> sessionList = messageSessionService.getSessionList(userId);
        return ApiResult.success(sessionList);
    }

    @PostMapping("/session")
    public ApiResult<HashMap<String, Integer>> addMessageSession(int toUserId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        int sessionId = messageSessionService.createSession(userId, toUserId);
        HashMap<String, Integer> resp = new HashMap<>();
        resp.put("sessionId", sessionId);
        return ApiResult.success(resp);
    }
}
