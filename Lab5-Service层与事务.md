# Lab 5: Service 层与 @Transactional 事务

## 学习目标
- 理解为什么要有 Service 层（三层架构的意义）
- 掌握 `@Service` 和 `@Transactional` 的所有属性
- 理解 DTO 模式（企业级必备）
- 掌握事务的核心概念：ACID、传播行为、隔离级别

---

## 核心概念：为什么要有 Service 层？

**没有 Service 层的错误写法（Controller 直接调 Repository）：**
```java
@RestController
public class BookController {
    @Autowired
    private BookRepository bookRepository;

    @PostMapping("/api/books/buy")
    public void buyBook(Long bookId, int quantity) {
        // 业务逻辑全塞在 Controller 里
        Book book = bookRepository.findById(bookId).orElseThrow();
        book.setStock(book.getStock() - quantity);  // 减库存
        bookRepository.save(book);
        // 如果这里还要记录购买记录呢？还要通知呢？Controller 越来越臃肿
    }
}
```

**三层架构的职责：**

```
请求 → Controller（接收参数，返回响应）
              ↓
          Service（处理业务逻辑，组合多个操作）
              ↓
        Repository（操作数据库）
```

| 层 | 职责 | 不应该做 |
|----|------|---------|
| Controller | 接收 HTTP 请求，参数校验，调用 Service，返回 HTTP 响应 | 包含业务逻辑 |
| Service | 业务逻辑，事务控制，跨 Repository 的操作 | 直接接触 HTTP |
| Repository | 数据库 CRUD | 包含业务逻辑 |

---

## 注解 1：`@Service`

```java
@Service  // = @Component，但语义上表示这是业务层
public class BookServiceImpl implements BookService {
    // ...
}
```

为什么推荐用接口 + 实现类的模式？
1. **可测试性**：测试时可以 Mock 接口，替换实现
2. **可扩展性**：未来换实现（比如从 JPA 换到 MyBatis）只改实现类
3. **大厂规范**：几乎所有大公司都要求 Service 有接口

---

## 注解 2：`@Transactional` — 事务管理

### 先理解事务的 ACID

**经典例子：银行转账**
```java
// A 转给 B 100 元
accountA.setBalance(accountA.getBalance() - 100);  // 步骤 1：A 扣钱
accountB.setBalance(accountB.getBalance() + 100);  // 步骤 2：B 加钱
```
如果步骤 1 成功，步骤 2 失败（服务器崩溃），A 的钱少了但 B 没收到 → 数据不一致！

**事务保证：两个步骤要么都成功，要么都回滚（全部失败），不会出现中间状态。**

| 特性 | 说明 |
|------|------|
| **A**tomicity（原子性） | 事务内的操作要么全成功，要么全失败回滚 |
| **C**onsistency（一致性） | 事务前后数据必须处于一致状态 |
| **I**solation（隔离性） | 并发事务之间互不影响 |
| **D**urability（持久性） | 事务提交后，数据永久保存 |

### 基本用法

```java
@Service
public class BookServiceImpl implements BookService {

    @Transactional           // ← 这个方法在一个事务里执行
    public Book save(Book book) {
        // 如果这里抛出 RuntimeException，之前的所有数据库操作都会回滚
        return bookRepository.save(book);
    }
}
```

### `@Transactional` 的所有重要属性

#### 1. `readOnly` — 只读事务（性能优化）

```java
// 查询操作：加 readOnly=true，数据库可以做优化（不记录 undo log）
@Transactional(readOnly = true)
public List<Book> findAll() {
    return bookRepository.findAll();
}

// 修改操作：不加（默认 readOnly=false）
@Transactional
public Book save(Book book) {
    return bookRepository.save(book);
}
```

**大厂规范：** 查询方法全加 `readOnly=true`，修改方法不加（或显式 `readOnly=false`）。

#### 2. `rollbackFor` — 指定回滚的异常类型

```java
// 重要！Spring 默认只在 RuntimeException 和 Error 时回滚
// 遇到 Checked Exception（如 IOException）不回滚！
// 建议：统一指定 Exception.class 回滚所有异常
@Transactional(rollbackFor = Exception.class)
public void importBooks(List<Book> books) throws IOException {
    // 如果这里抛出 IOException，会回滚（而默认行为是不回滚）
    for (Book book : books) {
        bookRepository.save(book);
    }
}

// 排除某个异常不回滚
@Transactional(noRollbackFor = StockWarningException.class)
public void decreaseStock(Long id, int quantity) {
    // 即使抛出 StockWarningException 也不回滚（警告级别，不影响数据）
}
```

#### 3. `propagation` — 事务传播行为

**场景：** 方法 A（有事务）调用方法 B（有事务），B 的事务怎么处理？

```java
// 最常用的三个传播行为：

// REQUIRED（默认）：有事务就加入，没有就新建
@Transactional(propagation = Propagation.REQUIRED)
public void methodB() {
    // 如果调用者 methodA 有事务，B 加入 A 的事务
    // 如果调用者没有事务，B 创建新事务
}

// REQUIRES_NEW：无论如何都新建事务（挂起当前事务）
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveLog(String message) {
    // 即使外层事务回滚，这里的日志也会提交
    // 常用于：记录审计日志（不能因业务回滚而丢失日志）
    logRepository.save(new Log(message));
}

// NOT_SUPPORTED：不在事务中运行（挂起当前事务）
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void sendEmail(String content) {
    // 发邮件不需要事务（且不应该在事务里，会拖慢事务）
}
```

**面试高频：传播行为表：**

| 传播行为 | 有事务时 | 没事务时 |
|---------|---------|---------|
| `REQUIRED`（默认） | 加入当前事务 | 新建事务 |
| `REQUIRES_NEW` | 挂起当前，新建事务 | 新建事务 |
| `SUPPORTS` | 加入当前事务 | 不用事务 |
| `NOT_SUPPORTED` | 挂起当前，不用事务 | 不用事务 |
| `MANDATORY` | 加入当前事务 | 抛异常 |
| `NEVER` | 抛异常 | 不用事务 |
| `NESTED` | 嵌套事务（SavePoint）| 新建事务 |

#### 4. `isolation` — 隔离级别

```java
// 默认（使用数据库默认级别，MySQL 是 REPEATABLE_READ）
@Transactional(isolation = Isolation.DEFAULT)

// 读已提交（读取其他事务已提交的数据）
@Transactional(isolation = Isolation.READ_COMMITTED)

// 可重复读（同一事务内多次读取结果一致）
@Transactional(isolation = Isolation.REPEATABLE_READ)

// 串行化（最高隔离级别，完全防止并发问题，但性能最差）
@Transactional(isolation = Isolation.SERIALIZABLE)
```

**隔离级别 vs 并发问题：**

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
|---------|------|-----------|------|
| READ_UNCOMMITTED | ✓可能 | ✓可能 | ✓可能 |
| READ_COMMITTED | ✗防止 | ✓可能 | ✓可能 |
| REPEATABLE_READ（MySQL默认）| ✗防止 | ✗防止 | ✓可能* |
| SERIALIZABLE | ✗防止 | ✗防止 | ✗防止 |

*MySQL 的 MVCC 机制在 REPEATABLE_READ 下也基本防止了幻读。

---

## 核心概念：DTO 模式

### 什么是 DTO？
**DTO（Data Transfer Object，数据传输对象）** 是专门用来在层之间传递数据的简单对象。

### 为什么不直接用实体类（Entity）传输？

```java
// 不好的做法：直接返回 Entity
@GetMapping("/{id}")
public Book getBook(@PathVariable Long id) {
    return bookRepository.findById(id).get();
    // 问题 1：密码字段（如果有）也会被序列化返回给前端！
    // 问题 2：JPA 的懒加载关联对象，序列化时可能触发额外查询或报错
    // 问题 3：API 结构和数据库结构强耦合，改数据库表就改了 API
}
```

**好的做法：用 DTO**

```
前端发送 → BookRequest（只含前端需要填写的字段）
                 ↓ Service 处理
           Book（Entity，数据库映射）
                 ↓ 转换
           BookResponse（只含前端需要看到的字段）→ 返回给前端
```

### 动手：创建 DTO 类

**创建 `src/main/java/com/example/bookshelf/dto/BookRequest.java`（接收前端数据）：**
```java
package com.example.bookshelf.dto;

// 前端创建/更新书籍时发送的数据
// 不包含 id、createdAt、updatedAt（这些由后端管理）
public class BookRequest {
    private String title;
    private String author;
    private String isbn;
    private String category;
    private java.math.BigDecimal price;
    private Integer stock;
    private java.time.LocalDate publishDate;

    // Getter/Setter（Lab 9 用 Lombok 简化）
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public java.time.LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(java.time.LocalDate publishDate) { this.publishDate = publishDate; }
}
```

**创建 `src/main/java/com/example/bookshelf/dto/BookResponse.java`（返回给前端）：**
```java
package com.example.bookshelf.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// 返回给前端的数据
// 可以按需选择包含哪些字段
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private LocalDate publishDate;
    private LocalDateTime createdAt;

    // 静态工厂方法：从 Entity 转成 DTO（常见写法）
    public static BookResponse from(com.example.bookshelf.entity.Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setCategory(book.getCategory());
        response.setPrice(book.getPrice());
        response.setStock(book.getStock());
        response.setPublishDate(book.getPublishDate());
        response.setCreatedAt(book.getCreatedAt());
        return response;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

### 升级 Service 使用 DTO

**修改 BookService 接口：**
```java
package com.example.bookshelf.service;

import com.example.bookshelf.dto.BookRequest;
import com.example.bookshelf.dto.BookResponse;
import java.util.List;

public interface BookService {
    List<BookResponse> findAll();
    BookResponse findById(Long id);
    BookResponse create(BookRequest request);
    BookResponse update(Long id, BookRequest request);
    void delete(Long id);
}
```

**修改 BookServiceImpl：**
```java
package com.example.bookshelf.service.impl;

import com.example.bookshelf.dto.BookRequest;
import com.example.bookshelf.dto.BookResponse;
import com.example.bookshelf.entity.Book;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)   // ← 查询加 readOnly
    public List<BookResponse> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(BookResponse::from)   // Entity → DTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElse(null);  // Lab 7 会改成抛异常
    }

    @Override
    @Transactional(rollbackFor = Exception.class)   // ← 修改操作，任何异常都回滚
    public BookResponse create(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setPublishDate(request.getPublishDate());
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        Book saved = bookRepository.save(book);
        return BookResponse.from(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return null;
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setUpdatedAt(LocalDateTime.now());
        return BookResponse.from(bookRepository.save(book));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }
}
```

---

## 面试考点

> **Q：@Transactional 默认在什么情况下回滚？**
> A：默认只在 `RuntimeException` 和 `Error` 时回滚，`Checked Exception`（如 `IOException`）不会自动回滚。大厂规范：统一加 `rollbackFor = Exception.class` 确保所有异常都回滚。

> **Q：@Transactional 加在接口方法上还是实现类方法上？**
> A：建议加在**实现类**的方法上。加在接口上理论可以，但 Spring AOP 的代理机制更推荐加在实现类上（避免歧义）。

> **Q：@Transactional 的自调用失效问题？**
> A：同一个类中，方法 A 调方法 B，如果方法 B 有 `@Transactional`，事务**不会生效**！因为 Spring AOP 通过代理实现事务，自调用绕过了代理。解决方案：把方法 B 移到另一个 Service，或通过 `ApplicationContext` 获取代理对象再调用。

> **Q：REQUIRED 和 REQUIRES_NEW 的区别？**
> A：`REQUIRED` 加入已有事务（共用一个事务，外层回滚则内层也回滚）。`REQUIRES_NEW` 挂起外层事务，新建独立事务（内层提交不受外层影响），常用于审计日志。

---

## 本章小结

你已经学会了：
- ✅ Service 层的职责（三层架构）
- ✅ `@Service`：标记业务层
- ✅ `@Transactional`：事务管理（readOnly、rollbackFor、propagation、isolation）
- ✅ 事务传播行为（REQUIRED vs REQUIRES_NEW）
- ✅ DTO 模式：Entity 和 API 数据分离

---

下一步 → [Lab 6: 数据校验](./Lab6-数据校验.md)
