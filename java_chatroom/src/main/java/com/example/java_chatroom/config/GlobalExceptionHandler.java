package com.example.java_chatroom.config;

import com.example.java_chatroom.entity.ApiResult;
import com.example.java_chatroom.entity.ResultCode;
import com.example.java_chatroom.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<?> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return ApiResult.error(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResult<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("[数据重复] {}", e.getMessage());
        return ApiResult.error(ResultCode.DUPLICATE_USERNAME);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception e) {
        log.error("[系统异常] ", e);
        return ApiResult.error(ResultCode.SERVER_ERROR);
    }
}
