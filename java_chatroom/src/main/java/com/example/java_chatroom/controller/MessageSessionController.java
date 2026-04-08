package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.MessageSession;
import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.service.MessageSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
public class MessageSessionController {

    @Resource
    private MessageSessionService messageSessionService;

    @GetMapping("/sessionList")
    public Object getMessageSessionList(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ArrayList<MessageSession>();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ArrayList<MessageSession>();
        }
        return messageSessionService.getSessionList(user.getUserId());
    }

    @PostMapping("/session")
    public Object addMessageSession(int toUserId, @SessionAttribute("user") User user) {
        int sessionId = messageSessionService.createSession(user.getUserId(), toUserId);
        HashMap<String, Integer> resp = new HashMap<>();
        resp.put("sessionId", sessionId);
        return resp;
    }
}
