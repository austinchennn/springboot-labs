# Lab 9: Lombok — 消灭样板代码

## 学习目标
- 理解 Lombok 解决了什么问题
- 掌握所有常用 Lombok 注解
- 用 Lombok 重构现有代码（实体类、DTO、Service）
- 理解 Lombok 的潜在陷阱

---

## Lombok 解决了什么问题？

**没有 Lombok 时，一个简单的 Book 类需要写：**
- 7 个字段
- 14 个 Getter/Setter 方法
- 1 个 toString 方法
- 1 个 equals 和 hashCode 方法
- 2-3 个构造方法

合计约 **100+ 行**，但实际有意义的代码只有 **7 行**（字段定义）！

**有了 Lombok：**
```java
@Data            // 自动生成 Getter、Setter、toString、equals、hashCode
@Builder         // 自动生成 Builder 模式
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private String author;
    // ...7 个字段
}
// 总共 15 行，代替了原来 100+ 行
```

---

## 前提：确保 Lombok 已安装

1. **pom.xml 中已有依赖**（Lab 0 已添加）
2. **IntelliJ IDEA 必须安装 Lombok 插件**（File → Settings → Plugins → 搜索 Lombok → Install）
3. **开启注解处理**（File → Settings → Build → Compiler → Annotation Processors → Enable）

---

## 注解 1：`@Getter` / `@Setter`

### 是什么？
自动为字段生成 Getter/Setter 方法。

```java
public class BookSimple {

    @Getter @Setter  // 只为这个字段生成
    private String title;

    @Getter          // 只生成 Getter，不生成 Setter（只读字段）
    private Long id;

    private String secret;  // 不加注解，不生成
}

// 也可以加在类上，对所有字段生效
@Getter
@Setter
public class BookSimple {
    private Long id;
    private String title;
    // 自动生成 getId、setId、getTitle、setTitle
}
```

---

## 注解 2：`@Data` — 最常用！

### 是什么？
等于 `@Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor`

```java
@Data
public class BookRequest {
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Integer stock;
}
// 自动生成：getTitle、setTitle、getAuthor...（所有 Getter/Setter）
// 自动生成：toString()（打印所有字段）
// 自动生成：equals() 和 hashCode()（基于所有字段）
// 自动生成：无参构造器（因为没有 final 字段）
```

### `@Data` 在 Entity 上要谨慎！

`@Data` 会生成 `equals` 和 `hashCode`，对 JPA 实体类有坑：

```java
// 危险：@Data 在 Entity 上
@Data
@Entity
public class Book {
    @Id
    private Long id;
    // 如果 id 为 null（新建未保存的对象），equals 和 hashCode 可能出问题
    // 集合去重、Set 中的行为可能异常
}

// 推荐的 Entity 写法：
@Getter
@Setter
@Entity
@Table(name = "books")
public class Book {
    // 手动写 equals 和 hashCode，只用 id 比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();  // 固定值，避免 null id 的问题
    }
}
```

**结论：Entity 只用 `@Getter + @Setter`，不用 `@Data`；DTO 可以放心用 `@Data`。**

---

## 注解 3：`@Builder` — Builder 模式

### 是什么？
自动生成 Builder 模式，让对象创建更清晰、可读性更好。

```java
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
}

// 使用 Builder 创建对象（链式调用，字段顺序任意，可读性极好）
BookResponse response = BookResponse.builder()
        .id(1L)
        .title("Java 编程思想")
        .author("Bruce Eckel")
        .price(new BigDecimal("89.00"))
        .build();

// 对比传统方式：
BookResponse response = new BookResponse();
response.setId(1L);
response.setTitle("Java 编程思想");
response.setAuthor("Bruce Eckel");
response.setPrice(new BigDecimal("89.00"));
// Builder 更清晰，特别是字段多的时候
```

### `@Builder.Default` — Builder 中的默认值

```java
@Builder
public class BookRequest {
    private String title;

    @Builder.Default               // ← 不加这个，builder 时这个字段会是 0，而不是 10
    private Integer stock = 10;    // 库存默认值 10
}
```

---

## 注解 4：`@NoArgsConstructor` / `@AllArgsConstructor` / `@RequiredArgsConstructor`

```java
@NoArgsConstructor      // 生成无参构造器（JPA 要求 Entity 必须有无参构造器！）
@AllArgsConstructor     // 生成包含所有字段的构造器
public class Book {
    private Long id;
    private String title;
    private String author;
}

// 生成的代码：
// public Book() {}
// public Book(Long id, String title, String author) { ... }
```

```java
// @RequiredArgsConstructor：只为 final 字段生成构造器（依赖注入最常用！）
@RequiredArgsConstructor    // ← 代替 @Autowired 构造器注入
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;   // ← final 字段
    private final BookshelfProperties properties;  // ← final 字段

    // Lombok 自动生成：
    // public BookServiceImpl(BookRepository bookRepository, BookshelfProperties properties) {
    //     this.bookRepository = bookRepository;
    //     this.properties = properties;
    // }

    // 不需要手写构造器！
}
```

**大厂黄金组合：`@RequiredArgsConstructor` + `final` 字段 = 最优依赖注入写法**

---

## 注解 5：`@Slf4j` — 日志记录

### 是什么？
自动注入 `log` 字段（SLF4J Logger），直接用来打日志。

```java
@Slf4j       // ← 等价于：private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookResponse create(BookRequest request) {
        log.info("创建书籍，ISBN: {}", request.getIsbn());   // {} 是占位符

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            log.warn("ISBN 已存在: {}", request.getIsbn());   // warn 级别
            throw new DuplicateIsbnException(request.getIsbn());
        }

        Book saved = bookRepository.save(convertToEntity(request));
        log.debug("书籍创建成功，ID: {}", saved.getId());    // debug 级别（详细信息）
        return BookResponse.from(saved);
    }
}
```

**日志级别（从低到高）：** `TRACE` < `DEBUG` < `INFO` < `WARN` < `ERROR`

- **DEBUG**：调试信息（方法入参、中间变量）
- **INFO**：重要操作记录（用户登录、数据创建）
- **WARN**：警告（库存不足，但操作成功）
- **ERROR**：错误（数据库连接失败、未处理异常）

---

## 注解 6：`@ToString`

```java
// 默认包含所有字段
@ToString
public class Book { ... }
// 输出：Book(id=1, title=Java 编程思想, author=Bruce Eckel, ...)

// 排除某些字段（如密码、大字段）
@ToString(exclude = {"password", "content"})
public class User { ... }

// 只包含某些字段
@ToString(onlyExplicitlyIncluded = true)
public class User {
    @ToString.Include private Long id;
    @ToString.Include private String username;
    private String password;  // 不包含
}
```

---

## 注解 7：`@EqualsAndHashCode`

```java
// 默认：基于所有非 static 字段
@EqualsAndHashCode
public class Book { ... }

// 只用 isbn 判断相等（业务意义上，ISBN 相同就是同一本书）
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {
    @EqualsAndHashCode.Include
    private String isbn;
    private String title;   // 不参与比较
}

// 排除某字段
@EqualsAndHashCode(exclude = "updatedAt")
public class Book { ... }
```

---

## 动手实践：用 Lombok 重构所有类

### 重构 `Book.java`（Entity）

```java
package com.example.bookshelf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    @Column(length = 50)
    private String category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

### 重构 `BookRequest.java`（DTO）

```java
package com.example.bookshelf.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data   // ← 一个注解代替所有 Getter/Setter/toString/equals/hashCode
public class BookRequest {

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名最长 200 字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 100, message = "作者名最长 100 字符")
    private String author;

    @NotBlank(message = "ISBN 不能为空")
    @Pattern(regexp = "^\\d{3}-\\d{10}$", message = "ISBN 格式不正确")
    private String isbn;

    @Size(max = 50, message = "分类名最长 50 字符")
    private String category;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格最低 0.01 元")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Past(message = "出版日期必须是过去的日期")
    private LocalDate publishDate;
}
```

### 重构 `BookResponse.java`（DTO）

```java
package com.example.bookshelf.dto;

import com.example.bookshelf.entity.Book;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
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

    // 静态工厂方法：Entity → DTO 转换
    public static BookResponse from(Book book) {
        return BookResponse.builder()   // ← Builder 方式构建，可读性好
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .category(book.getCategory())
                .price(book.getPrice())
                .stock(book.getStock())
                .publishDate(book.getPublishDate())
                .createdAt(book.getCreatedAt())
                .build();
    }
}
```

### 重构 `BookServiceImpl.java`

```java
package com.example.bookshelf.service.impl;

import com.example.bookshelf.config.BookshelfProperties;
import com.example.bookshelf.dto.BookRequest;
import com.example.bookshelf.dto.BookResponse;
import com.example.bookshelf.entity.Book;
import com.example.bookshelf.exception.BookNotFoundException;
import com.example.bookshelf.exception.DuplicateIsbnException;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.service.BookService;
import lombok.RequiredArgsConstructor;   // ← 自动生成构造器
import lombok.extern.slf4j.Slf4j;        // ← 自动注入 log
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor   // ← final 字段的构造器自动生成，不需要手写
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;   // final！
    private final BookshelfProperties properties;  // final！

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        log.debug("查询所有书籍");
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        log.debug("查询书籍，ID: {}", id);
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookResponse create(BookRequest request) {
        log.info("创建书籍，ISBN: {}", request.getIsbn());

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            log.warn("ISBN 已存在: {}", request.getIsbn());
            throw new DuplicateIsbnException(request.getIsbn());
        }

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
        log.info("书籍创建成功，ID: {}", saved.getId());
        return BookResponse.from(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookResponse update(Long id, BookRequest request) {
        log.info("更新书籍，ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

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
        log.info("删除书籍，ID: {}", id);
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }
}
```

---

## Lombok 常见陷阱

### 陷阱 1：`@Builder` 和 `@NoArgsConstructor` 冲突

```java
@Builder
@NoArgsConstructor   // ← 和 @Builder 同时用会报错！
@AllArgsConstructor  // ← 必须同时加这个才能解决
public class Book {
    private String title;
}
```

### 陷阱 2：`@Data` + 继承时的 `equals` 问题

```java
@Data
public class Animal {
    private String name;
}

@Data
@EqualsAndHashCode(callSuper = true)   // ← 必须加这个，否则子类 equals 不包含父类字段
public class Dog extends Animal {
    private String breed;
}
```

### 陷阱 3：循环引用导致 `toString` 死循环

```java
// Category 里有 List<Book>，Book 里有 Category
// 如果都加 @ToString，调用 toString 会无限递归！
@ToString(exclude = "books")   // ← 必须排除关联集合
public class Category {
    @OneToMany(mappedBy = "category")
    private List<Book> books;
}
```

---

## 面试考点

> **Q：Lombok 的 `@Data` 包含哪些注解？**
> A：`@Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor`

> **Q：为什么 JPA Entity 不建议用 `@Data`？**
> A：`@Data` 生成的 `equals` 和 `hashCode` 基于所有字段，JPA 实体的 `id` 在 persist 前是 null，会导致集合操作出现异常。建议 Entity 只用 `@Getter + @Setter`，手动实现基于 `id` 的 `equals` 和 `hashCode`。

> **Q：`@RequiredArgsConstructor` 生成什么构造器？**
> A：为所有 `final` 字段和标注了 `@NonNull` 的字段生成一个构造器。配合 Spring 的构造器注入，是最推荐的依赖注入方式。

---

## 本章小结

你已经学会了：
- ✅ `@Getter` / `@Setter`：生成字段访问方法
- ✅ `@Data`：DTO 类一键生成（Entity 慎用）
- ✅ `@Builder`：Builder 模式，对象构建更清晰
- ✅ `@NoArgsConstructor` / `@AllArgsConstructor` / `@RequiredArgsConstructor`
- ✅ `@Slf4j`：日志注入（企业必备）
- ✅ `@ToString` / `@EqualsAndHashCode`：自定义控制

---

下一步 → [Lab 10: AOP 切面编程](./Lab10-AOP切面.md)
