# Lab 11: 缓存、异步与定时任务

## 学习目标
- 掌握 Spring Cache：`@Cacheable`、`@CachePut`、`@CacheEvict`
- 掌握异步执行：`@Async`
- 掌握定时任务：`@Scheduled`
- 理解这些特性在大厂的实际使用场景

---

## Part 1：Spring Cache 缓存

### 为什么要缓存？

```java
// 没有缓存：
// 每次请求 /api/books/1 → 查数据库 → 返回数据
// 如果这本书每秒被请求 1000 次 → 数据库每秒执行 1000 次 SELECT

// 有缓存：
// 第一次请求 /api/books/1 → 查数据库 → 存入缓存 → 返回数据
// 之后的请求 /api/books/1 → 从缓存读 → 返回数据（不访问数据库！）
```

### 开启缓存

**修改 `BookshelfApplication.java`：**
```java
@SpringBootApplication
@EnableCaching   // ← 开启 Spring Cache 功能（必须加！）
public class BookshelfApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookshelfApplication.class, args);
    }
}
```

**`application.yml` 中配置缓存（默认用 ConcurrentHashMap，适合开发/单机）：**
```yaml
spring:
  cache:
    type: simple   # simple = 本地 ConcurrentHashMap（开发用）
    # 生产环境换 redis：type: redis
```

---

## 注解 1：`@Cacheable` — 读缓存

### 是什么？
方法第一次执行时，把返回值放入缓存。之后调用相同参数的方法，直接从缓存返回，**不执行方法体**。

```java
// value：缓存的名称（相当于缓存的命名空间）
// key：缓存的 key（SpEL 表达式）
@Cacheable(value = "books", key = "#id")
public BookResponse findById(Long id) {
    // 第一次调用：执行这里，结果存入缓存 books::1
    // 之后调用：不执行这里，直接从缓存 books::1 取
    return bookRepository.findById(id)
            .map(BookResponse::from)
            .orElseThrow(() -> new BookNotFoundException(id));
}
```

**SpEL（Spring Expression Language）key 的写法：**
```java
key = "#id"                    // 方法参数 id
key = "#request.isbn"          // 对象参数的字段
key = "'books:' + #id"         // 字符串拼接
key = "#root.methodName"       // 方法名
key = "T(java.time.LocalDate).now()"  // 静态方法调用
```

**条件缓存：**
```java
// 只有当 id > 0 时才缓存
@Cacheable(value = "books", key = "#id", condition = "#id > 0")
public BookResponse findById(Long id) { ... }

// 只有当结果不为 null 时才缓存
@Cacheable(value = "books", key = "#id", unless = "#result == null")
public BookResponse findById(Long id) { ... }
```

---

## 注解 2：`@CachePut` — 更新缓存

### 是什么？
**每次都执行方法**，同时用新的返回值**更新缓存**。用于数据更新时同步缓存。

```java
// 和 @Cacheable 的区别：
// @Cacheable：缓存命中时不执行方法
// @CachePut：每次都执行方法，并更新缓存

@CachePut(value = "books", key = "#id")  // 更新 books::id 的缓存
public BookResponse update(Long id, BookRequest request) {
    // 这里一定会执行
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    book.setTitle(request.getTitle());
    // ...
    return BookResponse.from(bookRepository.save(book));
    // 返回值会自动更新到缓存中
}
```

---

## 注解 3：`@CacheEvict` — 清除缓存

### 是什么？
**删除缓存**中的数据。删除操作后，对应的缓存要清除。

```java
// 删除 books::id 的缓存
@CacheEvict(value = "books", key = "#id")
public void delete(Long id) {
    bookRepository.deleteById(id);
}

// 清除整个 books 缓存空间的所有数据
@CacheEvict(value = "books", allEntries = true)
public void clearAllCache() { }

// 在方法执行前清除缓存（默认是执行后）
@CacheEvict(value = "books", key = "#id", beforeInvocation = true)
public void delete(Long id) { ... }
```

---

## 动手实践：给 BookService 加缓存

**修改 `BookServiceImpl.java`：**

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "#id", unless = "#result == null")
    public BookResponse findById(Long id) {
        log.info("从数据库查询书籍 ID: {}（如果你在日志里看到这条，说明没命中缓存）", id);
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CachePut(value = "books", key = "#id")   // 更新成功后，同步更新缓存
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setUpdatedAt(java.time.LocalDateTime.now());
        return BookResponse.from(bookRepository.save(book));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "books", key = "#id")  // 删除后清除对应缓存
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }
}
```

**测试缓存效果：**
```bash
# 第一次查询（查数据库，日志中会出现 SQL 和 "从数据库查询" 的日志）
curl http://localhost:8080/api/books/1

# 第二次查询同一个 ID（命中缓存，不查数据库，日志中不会出现 SQL）
curl http://localhost:8080/api/books/1

# 删除后再查询（缓存被清除，重新查数据库）
curl -X DELETE http://localhost:8080/api/books/1
curl http://localhost:8080/api/books/1  # 返回 404
```

### 生产环境用 Redis

```yaml
# application-prod.yml
spring:
  cache:
    type: redis
  redis:
    host: redis-server
    port: 6379
    password: ${REDIS_PASSWORD}
    timeout: 2000ms
  cache:
    redis:
      time-to-live: 3600000  # 缓存有效期 1 小时（毫秒）
```

---

## Part 2：异步执行 `@Async`

### 为什么要异步？

```java
// 同步（慢！）：
// 用户注册 → 保存用户 → 发送欢迎邮件（可能要 2 秒）→ 返回响应
// 用户等了 2 秒，体验很差

// 异步（快！）：
// 用户注册 → 保存用户 → 立即返回响应 → 另一个线程发送欢迎邮件
// 用户立即得到响应，邮件在后台发送
```

### 开启异步

```java
@SpringBootApplication
@EnableCaching
@EnableAsync   // ← 开启异步支持（必须加！）
public class BookshelfApplication { ... }
```

### 使用 `@Async`

**创建 `src/main/java/com/example/bookshelf/service/NotificationService.java`：**

```java
package com.example.bookshelf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationService {

    // @Async：这个方法在独立线程中异步执行
    // 调用方不等待这个方法完成，立即返回
    @Async
    public void sendWelcomeEmail(String email) {
        log.info("【异步】开始发送欢迎邮件给: {} (线程: {})",
                email, Thread.currentThread().getName());

        // 模拟发邮件耗时 2 秒
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        log.info("【异步】欢迎邮件发送成功: {}", email);
    }

    // @Async 方法可以返回 CompletableFuture（如果需要获取结果）
    @Async
    public CompletableFuture<String> sendEmailAndGetResult(String email) {
        // 模拟发送
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        return CompletableFuture.completedFuture("邮件已发送到 " + email);
    }

    // 模拟低库存通知
    @Async
    public void notifyLowStock(Long bookId, int stock) {
        log.warn("【异步通知】书籍 ID:{} 库存不足，当前库存: {}", bookId, stock);
        // 实际发送通知（短信、邮件、钉钉等）
    }
}
```

**在 Service 中使用：**

```java
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final NotificationService notificationService;  // 注入

    @Transactional(rollbackFor = Exception.class)
    public BookResponse create(BookRequest request) {
        // ... 创建书籍逻辑

        // 异步发送通知（不阻塞当前线程）
        // 这行立即返回，邮件在后台线程发送
        notificationService.sendWelcomeEmail("admin@example.com");

        return BookResponse.from(saved);
        // 这里已经返回了，邮件还在后台发送中！
    }
}
```

### 配置线程池（生产必须！）

默认 `@Async` 使用的是 `SimpleAsyncTaskExecutor`（每次创建新线程，不重用，生产环境不能用！）

```java
// 创建 src/main/java/com/example/bookshelf/config/AsyncConfig.java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")          // Bean 名称，@Async("taskExecutor") 时指定使用这个
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);          // 核心线程数
        executor.setMaxPoolSize(20);          // 最大线程数
        executor.setQueueCapacity(100);       // 队列容量
        executor.setThreadNamePrefix("Async-");  // 线程名前缀（日志里看到）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // 拒绝策略
        executor.initialize();
        return executor;
    }
}
```

### `@Async` 的重要限制

```java
// ❌ 自调用不生效（和 @Transactional 一样的问题）
@Service
public class BookService {
    public void methodA() {
        this.methodB();   // 直接调用，不走代理，@Async 不生效！
    }

    @Async
    public void methodB() { ... }
}

// ✅ 正确做法：从另一个 Bean 调用
@Service
@RequiredArgsConstructor
public class BookService {
    private final NotificationService notificationService;  // 另一个 Bean

    public void methodA() {
        notificationService.sendNotification();  // 走代理，@Async 生效
    }
}
```

---

## Part 3：定时任务 `@Scheduled`

### 开启定时任务

```java
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling   // ← 开启定时任务（必须加！）
public class BookshelfApplication { ... }
```

### 使用 `@Scheduled`

**创建 `src/main/java/com/example/bookshelf/service/ScheduledTaskService.java`：**

```java
package com.example.bookshelf.service;

import com.example.bookshelf.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    // ─────────────────────────────────────────────
    // 固定速率：每 60000ms（60秒）执行一次
    // 从应用启动时开始计时
    // ─────────────────────────────────────────────
    @Scheduled(fixedRate = 60000)
    public void checkLowStock() {
        log.info("定时任务：检查低库存书籍...");
        // 查询库存低于 10 的书
        bookRepository.findLowStockBooks(10).forEach(book -> {
            notificationService.notifyLowStock(book.getId(), book.getStock());
        });
    }

    // ─────────────────────────────────────────────
    // 固定延迟：上次执行完成后，等待 30000ms 再执行
    // 和 fixedRate 的区别：
    // fixedRate：不管上次是否执行完，到时间就执行（可能有并发）
    // fixedDelay：上次执行完后再等待（不会并发）
    // ─────────────────────────────────────────────
    @Scheduled(fixedDelay = 30000)
    public void cleanupExpiredCache() {
        log.debug("定时任务：清理过期缓存...");
    }

    // ─────────────────────────────────────────────
    // initialDelay：应用启动后等待 10 秒才开始执行（防止启动时负载过高）
    // ─────────────────────────────────────────────
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void generateDailyReport() {
        log.info("定时任务：生成日报...");
    }

    // ─────────────────────────────────────────────
    // Cron 表达式（最灵活！可以指定精确时间）
    // 格式：秒 分 时 日 月 周
    // ─────────────────────────────────────────────

    // 每天凌晨 2:00 执行（数据库备份、统计报表常用）
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyDatabaseBackup() {
        log.info("定时任务：数据库备份...");
    }

    // 每 5 分钟执行一次
    @Scheduled(cron = "0 */5 * * * ?")
    public void everyFiveMinutes() {
        log.debug("定时任务：每5分钟执行");
    }

    // 工作日（周一到周五）上午 9:00 执行
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void workdayMorningTask() {
        log.info("定时任务：工作日早9点执行");
    }
}
```

### Cron 表达式速查

```
┌───────────── 秒 (0-59)
│ ┌───────────── 分 (0-59)
│ │ ┌───────────── 时 (0-23)
│ │ │ ┌───────────── 日 (1-31)
│ │ │ │ ┌───────────── 月 (1-12 或 JAN-DEC)
│ │ │ │ │ ┌───────────── 周 (0-7 或 SUN-SAT，0和7都是周日)
│ │ │ │ │ │
* * * * * *

常用例子：
0 0 * * * ?      每小时整点
0 0 0 * * ?      每天零点
0 0 2 * * ?      每天凌晨2点
0 0 0 1 * ?      每月1号零点
0 */30 * * * ?   每30分钟
0 0 9-18 * * MON-FRI  工作日9点到18点每小时
```

**从配置文件读取 Cron 表达式（更灵活）：**
```yaml
# application.yml
app:
  schedule:
    low-stock-check-cron: "0 0/5 * * * ?"  # 每5分钟检查
```

```java
@Scheduled(cron = "${app.schedule.low-stock-check-cron}")
public void checkLowStock() { ... }
```

---

## Part 4：三个注解的启用注解总结

| 功能 | 注解 | 加在哪里 |
|------|------|---------|
| 缓存 | `@EnableCaching` | 启动类或配置类 |
| 异步 | `@EnableAsync` | 启动类或配置类 |
| 定时 | `@EnableScheduling` | 启动类或配置类 |

---

## 面试考点

> **Q：@Cacheable 的 condition 和 unless 的区别？**
> A：`condition` 在方法执行**前**判断，表达式为 true 才缓存（无法访问返回值）。`unless` 在方法执行**后**判断，表达式为 true 时**不缓存**（可以用 `#result` 访问返回值）。比如 `unless="#result==null"` 表示返回 null 时不缓存。

> **Q：@Async 使用默认线程池的问题？**
> A：默认的 `SimpleAsyncTaskExecutor` 每次都创建新线程，没有线程复用，在高并发下会导致线程爆炸。生产环境必须配置 `ThreadPoolTaskExecutor`，设置合理的核心线程数、最大线程数、队列大小和拒绝策略。

> **Q：@Scheduled 的 fixedRate 和 fixedDelay 的区别？**
> A：`fixedRate` 从上次**开始**时间算起，到固定时间再次执行（无论上次是否完成，若完成慢会并发执行）。`fixedDelay` 从上次**结束**时间算起，等待指定时间再执行（不会并发）。长时任务用 `fixedDelay` 更安全。

> **Q：Spring Cache 的缓存失效问题？**
> A：缓存和数据库不一致是主要问题。解决：更新时用 `@CachePut` 同步更新缓存，删除时用 `@CacheEvict` 清除缓存。还要设置合理的 TTL（缓存过期时间），即使不主动清除，缓存也会自动过期刷新。

---

## 本章小结

你已经学会了：
- ✅ `@EnableCaching` + `@Cacheable`：读缓存（避免重复查数据库）
- ✅ `@CachePut`：更新缓存
- ✅ `@CacheEvict`：清除缓存
- ✅ `@EnableAsync` + `@Async`：异步执行（不阻塞主线程）
- ✅ `@EnableScheduling` + `@Scheduled`：定时任务（fixedRate/fixedDelay/cron）
- ✅ 生产必备：自定义线程池、Redis 缓存

---

下一步 → [Lab 12: 面试总结](./Lab12-面试总结.md)
