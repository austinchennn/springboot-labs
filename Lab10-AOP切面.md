# Lab 10: AOP 切面编程

## 学习目标
- 理解 AOP 是什么，解决了什么问题
- 掌握 AOP 的核心概念（切面/连接点/切点/通知）
- 掌握 `@Aspect`、`@Before`、`@After`、`@Around`、`@AfterReturning`、`@AfterThrowing`
- 实战：编写日志切面和性能监控切面

---

## 核心概念：AOP 解决什么问题？

假设你的项目里每个 Service 方法都需要：
1. 打印日志（进入方法、退出方法）
2. 记录执行时间（性能监控）
3. 检查权限（鉴权）

**没有 AOP 时，你需要在每个方法里重复写：**
```java
public BookResponse findById(Long id) {
    log.info("方法开始：findById, 参数: {}", id);  // 重复代码！
    long start = System.currentTimeMillis();          // 重复代码！

    // 实际业务逻辑（只有这几行是有意义的）
    Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    BookResponse result = BookResponse.from(book);

    log.info("方法结束：findById, 耗时: {}ms", System.currentTimeMillis() - start);  // 重复代码！
    return result;
}
// 如果有 100 个方法，你要复制这些"框架代码" 100 次！
```

**有了 AOP：**
- 把重复的"横切关注点"（日志、性能监控、权限）抽离出来
- 用一个切面类统一处理
- 所有方法自动享受这些功能，不需要改任何业务代码！

---

## AOP 核心概念（4 个术语必须记）

```
 你的代码（目标对象）                AOP
 ─────────────────               ──────────────────────────
 BookService.findAll()    ←─── Aspect（切面）= 日志功能的类
 BookService.create()     ←─── Pointcut（切点）= 哪些方法需要增强
 BookService.update()     ←─── JoinPoint（连接点）= 方法执行的那个时机
                               Advice（通知）= 在什么时机执行什么代码
```

| 术语 | 是什么 | 例子 |
|------|--------|------|
| **Aspect（切面）** | 横切关注点的模块化 | 日志切面类 `LoggingAspect` |
| **JoinPoint（连接点）** | 可以被拦截的点 | Spring AOP 中指**方法执行** |
| **Pointcut（切点）** | 筛选哪些 JoinPoint | `execution(* com.example.*.service.*.*(..))` |
| **Advice（通知）** | 在切点处执行的代码 | 方法前打日志 |

**Advice（通知）的五种类型：**

| 通知 | 执行时机 |
|------|---------|
| `@Before` | 方法**执行前** |
| `@After` | 方法执行**后**（无论成功还是异常） |
| `@AfterReturning` | 方法**正常返回后** |
| `@AfterThrowing` | 方法**抛出异常后** |
| `@Around` | **包围**整个方法（最强大，可控制是否执行） |

---

## Pointcut 表达式语法

```java
// 最常用的表达式：execution
execution(修饰符? 返回类型 包名.类名.方法名(参数))

// * 表示任意，.. 表示任意多个

// 例子：
execution(* com.example.bookshelf.service.*.*(..))
//         ↑ 任意返回类型
//                  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 包名
//                                     ↑ 任意类名
//                                        ↑ 任意方法名
//                                          ↑↑ 任意参数

// 更多例子：
execution(public * com.example.bookshelf.service.BookService.*(..))  // BookService 所有 public 方法
execution(* com.example.bookshelf..*.*(..))           // bookshelf 包及子包的所有类所有方法
execution(* com.example.bookshelf.service.*.find*(..)) // service 包下所有以 find 开头的方法
execution(* *(Long))                                    // 所有只有一个 Long 参数的方法

// @annotation：拦截带有特定注解的方法
@annotation(org.springframework.transaction.annotation.Transactional)  // 所有有 @Transactional 的方法

// within：拦截某个类或包下所有方法
within(com.example.bookshelf.service.*)      // service 包下所有方法
```

---

## 动手实践：编写日志切面

**创建 `src/main/java/com/example/bookshelf/aspect/LoggingAspect.java`：**

```java
package com.example.bookshelf.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect      // ← 声明这是一个切面类
@Component   // ← 切面类也需要被 Spring 管理（注册为 Bean）
public class LoggingAspect {

    // ─────────────────────────────────────────────
    // 定义切点（可复用）
    // ─────────────────────────────────────────────
    @Pointcut("execution(* com.example.bookshelf.service..*.*(..))")
    public void serviceLayer() {}   // 切点方法，无需方法体，名字用于引用

    @Pointcut("execution(* com.example.bookshelf.controller..*.*(..))")
    public void controllerLayer() {}

    // ─────────────────────────────────────────────
    // @Before：方法执行前
    // ─────────────────────────────────────────────
    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.debug("→ 进入方法: {}.{}(), 参数: {}",
                className, methodName, Arrays.toString(args));
    }

    // ─────────────────────────────────────────────
    // @AfterReturning：方法正常返回后（能拿到返回值）
    // ─────────────────────────────────────────────
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.debug("← 方法返回: {}(), 结果: {}", methodName, result);
    }

    // ─────────────────────────────────────────────
    // @AfterThrowing：方法抛出异常后（能拿到异常）
    // ─────────────────────────────────────────────
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        log.error("✗ 方法异常: {}(), 异常: {}", methodName, exception.getMessage());
    }

    // ─────────────────────────────────────────────
    // @After：方法执行后（无论成功还是异常，相当于 finally）
    // ─────────────────────────────────────────────
    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        // 通常不用，@AfterReturning 和 @AfterThrowing 更精确
    }

    // ─────────────────────────────────────────────
    // @Around：最强大！包围整个方法，可以控制是否执行
    // 性能监控 + 日志 可以用 @Around 一次搞定
    // ─────────────────────────────────────────────
    @Around("controllerLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();
        log.info("【{}】{} 开始", className, methodName);

        try {
            // 执行目标方法（没有这行，目标方法不会执行！）
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("【{}】{} 完成，耗时 {}ms", className, methodName, duration);

            // 可以在这里修改返回值！
            return result;

        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("【{}】{} 异常，耗时 {}ms，原因: {}",
                    className, methodName, duration, e.getMessage());
            throw e;  // 重新抛出，不要吞掉异常！
        }
    }
}
```

---

## 动手实践：自定义注解 + AOP（高级用法）

大厂常见需求：某些方法需要特殊处理（审计日志、权限检查），但不是所有方法都要。
解决方案：自定义注解 + AOP 拦截有该注解的方法。

### 步骤 1：定义自定义注解

**创建 `src/main/java/com/example/bookshelf/annotation/AuditLog.java`：**

```java
package com.example.bookshelf.annotation;

import java.lang.annotation.*;

// 标记需要记录审计日志的方法
@Target(ElementType.METHOD)         // ← 只能用在方法上
@Retention(RetentionPolicy.RUNTIME) // ← 运行时有效（AOP 需要）
@Documented
public @interface AuditLog {
    String action() default "";  // 操作描述，如 "创建书籍"
}
```

### 步骤 2：在需要的方法上使用注解

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(action = "创建书籍")   // ← 标记需要审计日志
    public BookResponse create(BookRequest request) {
        // 业务逻辑...
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(action = "删除书籍")   // ← 标记需要审计日志
    public void delete(Long id) {
        // 业务逻辑...
    }
}
```

### 步骤 3：AOP 拦截注解

**创建 `src/main/java/com/example/bookshelf/aspect/AuditLogAspect.java`：**

```java
package com.example.bookshelf.aspect;

import com.example.bookshelf.annotation.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    // 拦截所有有 @AuditLog 注解的方法
    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String action = auditLog.action();
        String methodName = joinPoint.getSignature().getName();

        // 审计日志记录（实际项目中会写入数据库 audit_log 表）
        log.info("【审计日志】操作: {}, 方法: {}, 时间: {}",
                action, methodName, LocalDateTime.now());

        try {
            Object result = joinPoint.proceed();
            log.info("【审计日志】操作: {} - 成功", action);
            return result;
        } catch (Throwable e) {
            log.error("【审计日志】操作: {} - 失败，原因: {}", action, e.getMessage());
            throw e;
        }
    }
}
```

---

## AOP 通知执行顺序

```
目标方法执行流程（有 @Around）：

@Around 开始
    ↓
  @Before
    ↓
  目标方法执行
    ↓（正常）          ↓（异常）
  @AfterReturning    @AfterThrowing
    ↓                   ↓
         @After（always）
              ↓
       @Around 结束
```

**当多个切面拦截同一个方法，顺序可以用 `@Order(n)` 控制（n 越小优先级越高）：**
```java
@Aspect
@Component
@Order(1)   // ← 第一个执行（优先级最高）
public class SecurityAspect { ... }

@Aspect
@Component
@Order(2)   // ← 第二个执行
public class LoggingAspect { ... }
```

---

## 面试考点

> **Q：Spring AOP 和 AspectJ 的区别？**
> A：Spring AOP 基于**动态代理**（JDK 动态代理或 CGLIB），只能拦截 Spring Bean 的方法调用，是运行时增强。AspectJ 是在编译时或类加载时织入，功能更强（可拦截字段访问、构造器等），但配置更复杂。Spring 项目中通常用 Spring AOP 就够了。

> **Q：@Before、@After、@Around 的区别，什么时候用哪个？**
> A：`@Before` 方法前执行。`@After` 方法后执行（类似 finally）。`@Around` 包围整个方法，能控制是否执行目标方法、修改入参和返回值。需要计算执行时间用 `@Around`；简单的前置/后置处理用 `@Before`/`@AfterReturning`。

> **Q：AOP 的自调用问题是什么？**
> A：同一个类的方法 A 调方法 B，如果 B 上有 AOP 注解（包括 `@Transactional`），AOP **不会生效**！因为 A 直接调用了对象本身，没有走代理。解决：把 B 移到另一个 Bean，或通过 `AopContext.currentProxy()` 获取代理对象。

> **Q：JDK 动态代理和 CGLIB 代理的区别？**
> A：JDK 动态代理要求目标类实现接口，通过接口生成代理。CGLIB 通过继承目标类生成子类代理，不要求接口。Spring Boot 默认用 CGLIB（`@EnableAspectJAutoProxy(proxyTargetClass=true)`）。

---

## 本章小结

你已经学会了：
- ✅ AOP 四个核心概念：切面、连接点、切点、通知
- ✅ `@Aspect` + `@Component`：定义切面类
- ✅ `@Before`：前置通知
- ✅ `@AfterReturning`：正常返回后通知
- ✅ `@AfterThrowing`：异常后通知
- ✅ `@After`：最终通知（类似 finally）
- ✅ `@Around`：环绕通知（最强大，可控制执行）
- ✅ Pointcut 表达式语法
- ✅ 自定义注解 + AOP 实现审计日志

---

下一步 → [Lab 11: 缓存与异步](./Lab11-缓存与异步.md)
