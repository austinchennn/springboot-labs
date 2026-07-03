package com.example.bookshelf.aspect;

// 统一的日志/性能监控切面（Lab10 "编写日志切面"）
// TODO(Lab10): 加上 @Aspect 和 @Component 注解
public class LoggingAspect {

    // ─────────────────────────────────────────────
    // 定义可复用的切点
    // ─────────────────────────────────────────────
    // TODO(Lab10): 加上 @Pointcut("execution(* com.example.bookshelf.service..*.*(..))")
    public void serviceLayer() {}

    // TODO(Lab10): 加上 @Pointcut("execution(* com.example.bookshelf.controller..*.*(..))")
    public void controllerLayer() {}

    // ─────────────────────────────────────────────
    // @Before：方法执行前打印日志
    // ─────────────────────────────────────────────
    // TODO(Lab10): 加上 @Before("serviceLayer()")，方法参数用 JoinPoint joinPoint
    // 打印类名、方法名、参数（参考 Lab10-AOP切面.md）
    public void logBefore() {
        throw new UnsupportedOperationException("TODO: 实现 logBefore");
    }

    // ─────────────────────────────────────────────
    // @AfterReturning：方法正常返回后，能拿到返回值
    // ─────────────────────────────────────────────
    // TODO(Lab10): 加上 @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    // 方法参数：JoinPoint joinPoint, Object result
    public void logAfterReturning() {
        throw new UnsupportedOperationException("TODO: 实现 logAfterReturning");
    }

    // ─────────────────────────────────────────────
    // @AfterThrowing：方法抛出异常后，能拿到异常
    // ─────────────────────────────────────────────
    // TODO(Lab10): 加上 @AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
    // 方法参数：JoinPoint joinPoint, Throwable exception
    public void logAfterThrowing() {
        throw new UnsupportedOperationException("TODO: 实现 logAfterThrowing");
    }

    // ─────────────────────────────────────────────
    // @Around：最强大，包围整个方法，用来做耗时统计
    // ─────────────────────────────────────────────
    // TODO(Lab10): 加上 @Around("controllerLayer()")
    // 方法参数：ProceedingJoinPoint joinPoint，返回类型 Object，方法签名 throws Throwable
    // 提示：记录 System.currentTimeMillis()，调用 joinPoint.proceed() 执行目标方法，
    // 用 try/catch 包裹，异常要重新 throw，不能吞掉
    public void logAround() {
        throw new UnsupportedOperationException("TODO: 实现 logAround");
    }
}
