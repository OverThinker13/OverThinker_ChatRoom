package com.example.java_chatroom.service.impl;

import com.example.java_chatroom.entity.Message;
import com.example.java_chatroom.mapper.MessageMapper;
import com.example.java_chatroom.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Override
    public List<Message> getMessageList(int sessionId) {
        List<Message> messages = messageMapper.getMessageBySessionId(sessionId);
        Collections.reverse(messages);
        return messages;
    }
}
