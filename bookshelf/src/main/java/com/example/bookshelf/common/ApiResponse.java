package com.example.bookshelf.common;

// 统一 API 响应包装类：所有接口的返回值都包在这里面（Lab7 "设计统一响应格式"）
// TODO(Lab7): 实现下面几个静态工厂方法和构造器
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // TODO(Lab7): 成功响应 —— return new ApiResponse<>(200, "操作成功", data);
    public static <T> ApiResponse<T> success(T data) {
        throw new UnsupportedOperationException("TODO: 实现 success(data)");
    }

    // TODO(Lab7): 成功响应（自定义消息）
    public static <T> ApiResponse<T> success(String message, T data) {
        throw new UnsupportedOperationException("TODO: 实现 success(message, data)");
    }

    // TODO(Lab7): 创建成功（201）—— return new ApiResponse<>(201, "创建成功", data);
    public static <T> ApiResponse<T> created(T data) {
        throw new UnsupportedOperationException("TODO: 实现 created(data)");
    }

    // TODO(Lab7): 失败响应 —— return new ApiResponse<>(code, message, null);
    public static <T> ApiResponse<T> error(int code, String message) {
        throw new UnsupportedOperationException("TODO: 实现 error(code, message)");
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
