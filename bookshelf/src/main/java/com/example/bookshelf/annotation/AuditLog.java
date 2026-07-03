package com.example.bookshelf.annotation;

import java.lang.annotation.*;

// 自定义注解：标记需要记录审计日志的方法（Lab10 "自定义注解 + AOP"）
// TODO(Lab10): 加上 @Target(ElementType.METHOD)、@Retention(RetentionPolicy.RUNTIME)、@Documented
public @interface AuditLog {

    // 操作描述，例如 "创建书籍"
    String action() default "";
}
