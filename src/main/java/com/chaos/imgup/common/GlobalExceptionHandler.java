package com.chaos.imgup.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        // 这里应该记录日志
        e.printStackTrace();
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }

    // 在这里添加更多针对特定异常的处理方法
    // 例如：@ExceptionHandler(BusinessException.class)
}
