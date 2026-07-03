package com.example.bookshelf.exception;

import com.example.bookshelf.common.ApiResponse;

import java.util.Map;

// TODO(Lab7): 加上 @RestControllerAdvice 注解，全局拦截所有 Controller 抛出的异常
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────
    // 处理 BookNotFoundException（404）
    // ─────────────────────────────────────────────
    // TODO(Lab7): 加上 @ExceptionHandler(BookNotFoundException.class)
    // 返回 ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, ex.getMessage()))
    public Object handleBookNotFound(BookNotFoundException ex) {
        throw new UnsupportedOperationException("TODO: 实现 handleBookNotFound");
    }

    // ─────────────────────────────────────────────
    // 处理 DuplicateIsbnException（409 Conflict）
    // ─────────────────────────────────────────────
    // TODO(Lab7): 加上 @ExceptionHandler(DuplicateIsbnException.class)
    // 返回 409 + ApiResponse.error(409, ex.getMessage())
    public Object handleDuplicateIsbn(DuplicateIsbnException ex) {
        throw new UnsupportedOperationException("TODO: 实现 handleDuplicateIsbn");
    }

    // ─────────────────────────────────────────────
    // 处理 @Valid 校验失败（400），异常类型是 MethodArgumentNotValidException
    // ─────────────────────────────────────────────
    // TODO(Lab7): 加上 @ExceptionHandler(MethodArgumentNotValidException.class)
    // 提示：遍历 ex.getBindingResult().getAllErrors()，把每个 FieldError 的字段名和错误信息
    // 收集进一个 Map<String, String>，包装成 ApiResponse<Map<String,String>> 返回 400
    public Object handleValidationErrors(Exception ex) {
        throw new UnsupportedOperationException("TODO: 实现 handleValidationErrors");
    }

    // ─────────────────────────────────────────────
    // 兜底处理：所有未预期的异常（500）
    // ─────────────────────────────────────────────
    // TODO(Lab7): 加上 @ExceptionHandler(Exception.class)
    // 返回 500 + ApiResponse.error(500, "服务器内部错误，请联系管理员")
    // 注意：生产环境不要把 ex.getMessage() 暴露给前端！
    public Object handleAllExceptions(Exception ex) {
        throw new UnsupportedOperationException("TODO: 实现 handleAllExceptions");
    }
}
