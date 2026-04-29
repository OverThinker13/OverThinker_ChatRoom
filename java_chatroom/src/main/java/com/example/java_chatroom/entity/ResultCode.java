package com.example.java_chatroom.entity;

public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    DUPLICATE_USERNAME(400, "用户名已存在"),
    LOGIN_FAILED(400, "用户名或密码错误"),
    FRIEND_REQUEST_FAILED(400, "好友请求失败"),
    REQUEST_NOT_FOUND(400, "请求不存在或无权限"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
