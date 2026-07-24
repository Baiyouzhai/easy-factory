package com.byz.factory.web.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * 统一 API 响应体。
 * <p>
 * 所有 REST 接口返回此格式的数据，确保前端处理的一致性。
 *
 * <pre>{@code
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { ... },
 *   "timestamp": "2026-07-12T10:30:00Z",
 *   "traceId": "a1b2c3d4"
 * }
 * }</pre>
 *
 * @param <T> 响应数据类型
 * @author 苏政
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 成功状态码 */
    public static final int CODE_OK = 200;
    /** 客户端错误基准 */
    public static final int CODE_BAD_REQUEST = 400;
    /** 服务端错误基准 */
    public static final int CODE_INTERNAL_ERROR = 500;

    private int code;
    private String message;
    private T data;
    private Instant timestamp;
    private String traceId;

    public Result() {
    }

    protected Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
        this.traceId = UUID.randomUUID().toString().substring(0, 8);
    }

    // ── 工厂方法 ──

    /**
     * 成功，带数据。
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(CODE_OK, "success", data);
    }

    /**
     * 成功，无数据。
     */
    public static <T> Result<T> ok() {
        return new Result<>(CODE_OK, "success", null);
    }

    /**
     * 业务失败（指定错误码和消息）。
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 业务失败（默认 400）。
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(CODE_BAD_REQUEST, message, null);
    }

    /**
     * 服务端错误（500）。
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(CODE_INTERNAL_ERROR, message, null);
    }

    // ── getter / setter ──

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

}
