package com.example.java_chatroom.controller;

import com.example.java_chatroom.entity.ApiResult;
import com.example.java_chatroom.entity.Message;
import com.example.java_chatroom.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
public class MessageController {

    @Resource
    private MessageService messageService;

    @GetMapping("/message")
    public ApiResult<List<Message>> getMessage(int sessionId) {
        List<Message> messages = messageService.getMessageList(sessionId);
        return ApiResult.success(messages);
    }
}
