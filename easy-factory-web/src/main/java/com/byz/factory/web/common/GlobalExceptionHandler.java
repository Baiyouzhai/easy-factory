package com.byz.factory.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * <p>
 * 将异常统一转换为 {@link Result} 格式，避免异常信息直接暴露给前端。
 *
 * @author 苏政
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 参数校验异常 — 400 Bad Request。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数校验异常: {}", e.getMessage());
        return Result.fail(Result.CODE_BAD_REQUEST, e.getMessage());
    }

    /**
     * 状态冲突异常 — 409 Conflict（如非法状态转换）。
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleIllegalState(IllegalStateException e) {
        log.warn("状态冲突异常: {}", e.getMessage());
        return Result.fail(409, e.getMessage());
    }

    /**
     * 未指定异常 — 500 Internal Server Error。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGeneral(Exception e) {
        log.error("未预期异常", e);
        return Result.error("服务器内部错误: " + e.getMessage());
    }

}
