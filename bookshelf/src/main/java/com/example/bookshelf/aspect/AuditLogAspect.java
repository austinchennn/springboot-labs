package com.example.bookshelf.aspect;

import com.example.bookshelf.annotation.AuditLog;

// 拦截所有标注了 @AuditLog 的方法，记录审计日志（Lab10 "自定义注解 + AOP"）
// TODO(Lab10): 加上 @Aspect 和 @Component 注解
public class AuditLogAspect {

    // TODO(Lab10): 加上 @Around("@annotation(auditLog)")
    // 方法参数：ProceedingJoinPoint joinPoint, AuditLog auditLog；返回 Object；throws Throwable
    // 提示：从 auditLog.action() 拿到操作描述，执行前后各打一条日志，
    // 异常时也要记录并重新 throw
    public Object audit() {
        throw new UnsupportedOperationException("TODO: 实现 audit");
    }
}
