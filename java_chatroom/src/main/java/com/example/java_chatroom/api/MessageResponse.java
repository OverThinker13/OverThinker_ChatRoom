package com.example.java_chatroom.api;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

// 表示一个响应
public class MessageResponse {

    private String type = "message";
    private int fromId;
    private String fromName;
    private int sessionId;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date postTime;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }
}
