# Lab 4: 数据持久化 — Spring Data JPA

## 学习目标
- 理解 JPA 是什么，为什么用它
- 掌握实体类注解：`@Entity`、`@Table`、`@Id`、`@GeneratedValue`、`@Column`
- 掌握关系映射：`@ManyToOne`、`@OneToMany`、`@JoinColumn`
- 掌握 `@Repository` 和 `JpaRepository`
- 掌握自定义查询：`@Query` 和方法命名规则

---

## 核心概念：什么是 JPA？

**传统 JDBC 写法（繁琐、易出错）：**
```java
Connection conn = DriverManager.getConnection(url, user, password);
PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE id = ?");
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
Book book = new Book();
book.setId(rs.getLong("id"));
book.setTitle(rs.getString("title"));
// 每个字段都要手动映射...
```

**JPA 写法（简洁！）：**
```java
bookRepository.findById(id);  // 就这一行！
```

**JPA（Java Persistence API）** 是 Java 的 ORM 规范，把 Java 对象和数据库表做映射，让你用面向对象的方式操作数据库，不需要手写 SQL。

**Spring Data JPA** 是对 JPA 的进一步封装，提供了更便捷的 Repository 接口。

**底层实现**：Spring Data JPA 默认使用 **Hibernate** 作为 JPA 的具体实现。

---

## 注解 1：`@Entity`

### 是什么？
标记一个 Java 类是数据库表的映射（即"实体类"）。Spring Data JPA 会为它在数据库中创建对应的表。

```java
@Entity  // 告诉 JPA：这个类对应数据库的一张表
public class Book {
    // ...
}
```

---

## 注解 2：`@Table`

### 是什么？
指定实体类对应的**数据库表名**。如果不加，默认表名就是类名（Book → book 表）。

```java
@Entity
@Table(name = "books")  // 指定表名为 "books"（推荐：表名用复数）
public class Book {
    // ...
}
```

---

## 注解 3：`@Id` 和 `@GeneratedValue`

### 是什么？
- `@Id`：标记这个字段是数据库表的**主键**
- `@GeneratedValue`：指定主键的**生成策略**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**主键生成策略：**

| 策略 | 说明 | 使用场景 |
|------|------|---------|
| `IDENTITY` | 数据库自增（AUTO_INCREMENT）| MySQL、H2 **最常用** |
| `SEQUENCE` | 数据库序列 | Oracle、PostgreSQL |
| `AUTO` | JPA 自动选择 | 不推荐（不可控） |
| `TABLE` | 用单独的表记录 ID | 少用 |

**大厂常用：** MySQL 用 `IDENTITY`，分布式场景用雪花算法 ID（需要自定义）。

---

## 注解 4：`@Column`

### 是什么？
指定字段对应的数据库**列**的属性。不加时，默认列名就是字段名（驼峰转下划线）。

```java
@Column(
    name = "book_title",          // 列名（默认：title）
    nullable = false,             // 不允许 NULL（DEFAULT: true）
    unique = false,               // 是否唯一（DEFAULT: false）
    length = 200,                 // VARCHAR 长度（DEFAULT: 255）
    updatable = true,             // 是否允许更新（DEFAULT: true）
    insertable = true             // 是否允许插入（DEFAULT: true）
)
private String title;

// 数字精度
@Column(precision = 10, scale = 2)  // DECIMAL(10, 2)，即 99999999.99
private BigDecimal price;
```

---

## 动手实践：升级 Book 实体类

**修改 `src/main/java/com/example/bookshelf/entity/Book.java`（全量替换）：**

```java
package com.example.bookshelf.entity;

import jakarta.persistence.*;          // Spring Boot 3.x 用 jakarta（不是 javax）
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity                               // ← 这是 JPA 实体类
@Table(name = "books")                // ← 对应数据库 books 表
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← 主键自增
    private Long id;

    @Column(nullable = false, length = 200)              // ← 不允许 NULL，最长 200 字符
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(unique = true, nullable = false, length = 20) // ← ISBN 唯一
    private String isbn;

    @Column(length = 50)
    private String category;

    @Column(precision = 10, scale = 2)                   // ← DECIMAL(10,2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "publish_date")                       // ← 列名用下划线命名
    private LocalDate publishDate;

    @Column(name = "created_at", updatable = false)      // ← 创建时间，不允许更新
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 手写 Getter/Setter（Lab 9 用 @Data 代替）
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
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

---

## 注解 5：`@Repository` 和 `JpaRepository`

### 是什么？
`@Repository` 是 `@Component` 的特化，标记数据访问层。
`JpaRepository` 是 Spring Data JPA 提供的接口，继承它就自动获得大量 CRUD 方法，**不需要写任何实现！**

**创建文件：`src/main/java/com/example/bookshelf/repository/BookRepository.java`**
```java
package com.example.bookshelf.repository;

import com.example.bookshelf.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 继承 JpaRepository<实体类型, 主键类型>
// Spring Data JPA 自动生成实现类，不需要你写任何代码！
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // JpaRepository 已经提供了：
    // save(entity)          → 保存或更新（有 id 是更新，无 id 是新增）
    // findById(id)          → 根据 id 查询，返回 Optional<Book>
    // findAll()             → 查询所有
    // deleteById(id)        → 根据 id 删除
    // existsById(id)        → id 是否存在
    // count()               → 统计总数
    // 以及更多...
}
```

### 方法命名规则（Spring Data 的魔法）

只要你在 Repository 接口里**按规则命名方法**，Spring Data JPA 会**自动生成 SQL**！

```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // 根据作者查询
    // → SELECT * FROM books WHERE author = ?
    List<Book> findByAuthor(String author);

    // 根据分类查询
    // → SELECT * FROM books WHERE category = ?
    List<Book> findByCategory(String category);

    // 根据书名模糊搜索（Containing = LIKE %keyword%）
    // → SELECT * FROM books WHERE title LIKE '%keyword%'
    List<Book> findByTitleContaining(String keyword);

    // 根据价格区间查询（Between）
    // → SELECT * FROM books WHERE price BETWEEN ? AND ?
    List<Book> findByPriceBetween(BigDecimal min, BigDecimal max);

    // 根据分类查询，按价格升序排序（OrderBy...Asc）
    // → SELECT * FROM books WHERE category = ? ORDER BY price ASC
    List<Book> findByCategoryOrderByPriceAsc(String category);

    // 判断 ISBN 是否存在
    // → SELECT COUNT(*) > 0 FROM books WHERE isbn = ?
    boolean existsByIsbn(String isbn);

    // 根据分类统计数量
    // → SELECT COUNT(*) FROM books WHERE category = ?
    long countByCategory(String category);

    // 根据作者和分类查询（And）
    List<Book> findByAuthorAndCategory(String author, String category);

    // 根据书名或作者搜索（Or）
    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);
}
```

**方法命名规则关键词：**

| 关键词 | SQL | 例子 |
|--------|-----|------|
| `findBy` | SELECT WHERE | `findByTitle` |
| `countBy` | COUNT WHERE | `countByCategory` |
| `existsBy` | EXISTS WHERE | `existsByIsbn` |
| `deleteBy` | DELETE WHERE | `deleteByIsbn` |
| `And` | AND | `findByTitleAndAuthor` |
| `Or` | OR | `findByTitleOrAuthor` |
| `Containing` | LIKE %?% | `findByTitleContaining` |
| `StartingWith` | LIKE ?% | `findByTitleStartingWith` |
| `EndingWith` | LIKE %? | `findByTitleEndingWith` |
| `Between` | BETWEEN | `findByPriceBetween` |
| `LessThan` | < | `findByPriceLessThan` |
| `GreaterThan` | > | `findByPriceGreaterThan` |
| `OrderBy...Asc` | ORDER BY ASC | `findByCategoryOrderByPriceAsc` |
| `OrderBy...Desc` | ORDER BY DESC | `findByCategoryOrderByPriceDesc` |

---

## 注解 6：`@Query` — 自定义 JPQL 查询

当方法命名满足不了需求时，用 `@Query` 写自定义查询：

```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // JPQL（类似 SQL，但用类名和字段名，不用表名和列名）
    // :title 是命名参数，用 @Param 绑定
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> searchByKeyword(@Param("keyword") String keyword);

    // 原生 SQL（nativeQuery = true，用表名和列名）
    @Query(value = "SELECT * FROM books WHERE stock < :threshold", nativeQuery = true)
    List<Book> findLowStockBooks(@Param("threshold") int threshold);

    // 分页查询（配合 Pageable）
    @Query("SELECT b FROM Book b WHERE b.category = :category")
    Page<Book> findByCategoryPaged(@Param("category") String category, Pageable pageable);
}
```

**JPQL vs 原生 SQL：**
- **JPQL**：面向对象，用 Java 类名（`Book`）和字段名（`title`），数据库无关
- **原生 SQL**：用真实表名（`books`）和列名（`title`），数据库相关，但灵活

---

## 关系映射注解

### `@ManyToOne` 和 `@OneToMany`

假设一个分类（Category）有多本书（Book），一本书属于一个分类。

**创建 Category 实体（演示用）：**
```java
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 一个分类有多本书（mappedBy 指向 Book 中的 category 字段）
    // 这一侧不维护外键（外键在 Book 表）
    @OneToMany(mappedBy = "category",
               cascade = CascadeType.ALL,  // 级联操作：删除分类时删除所有书
               fetch = FetchType.LAZY)     // 懒加载：只有访问 books 时才查询（重要！）
    private List<Book> books;

    // getter/setter...
}
```

**修改 Book 实体，建立多对一关系（这里简化为字符串，仅演示注解）：**
```java
// 如果 Book 关联 Category 对象：
@ManyToOne(fetch = FetchType.LAZY)         // 多本书 → 一个分类
@JoinColumn(name = "category_id",          // books 表中的外键列名
            nullable = false)
private Category category;
```

### `FetchType` 很重要！

| | `EAGER`（急加载） | `LAZY`（懒加载） |
|--|----------------|----------------|
| 时机 | 主表查询时立即查子表 | 访问子表字段时才查询 |
| 默认 | `@ManyToOne`、`@OneToOne` | `@OneToMany`、`@ManyToMany` |
| 推荐 | **全都用 LAZY** | **大厂规范：全 LAZY 按需加载** |

**N+1 问题警告：** 如果查询列表时触发懒加载，会产生 N 条额外 SQL（查 1 次主表 + N 次子表）。解决方案：用 `@Query` + JOIN FETCH，或 DTO 投影。

---

## 动手实践：让 API 连接数据库

**创建 `src/main/java/com/example/bookshelf/service/BookService.java`（接口）：**
```java
package com.example.bookshelf.service;

import com.example.bookshelf.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> findAll();
    Book findById(Long id);
    Book save(Book book);
    Book update(Long id, Book book);
    void delete(Long id);
}
```

**创建 `src/main/java/com/example/bookshelf/service/impl/BookServiceImpl.java`：**
```java
package com.example.bookshelf.service.impl;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.service.BookService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book findById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Book update(Long id, Book updatedBook) {
        Book existing = bookRepository.findById(id).orElse(null);
        if (existing == null) return null;
        existing.setTitle(updatedBook.getTitle());
        existing.setAuthor(updatedBook.getAuthor());
        existing.setPrice(updatedBook.getPrice());
        existing.setStock(updatedBook.getStock());
        return bookRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }
}
```

**修改 `BookController.java`，使用 Service 而不是内存列表：**
```java
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.findById(id);
        return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book saved = bookService.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        Book updated = bookService.update(id, book);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**添加初始数据（添加到 `application.yml`）：**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  sql:
    init:
      mode: always
```

**创建 `src/main/resources/data.sql`（程序启动时自动执行）：**
```sql
INSERT INTO books (title, author, isbn, category, price, stock) VALUES
('Java 编程思想', 'Bruce Eckel', '978-0131872494', '技术', 89.00, 100),
('深入理解 Java 虚拟机', '周志明', '978-7111641247', '技术', 129.00, 50),
('三体', '刘慈欣', '978-7536692930', '科幻', 45.00, 200);
```

---

## 面试考点

> **Q：JPA 中 `@Column(nullable=false)` 和数据库约束有什么区别？**
> A：`@Column(nullable=false)` 让 JPA/Hibernate 在生成建表语句时加 NOT NULL 约束，同时 Hibernate 也会在插入前进行验证。结合 `@NotNull` 可以在应用层也进行校验（Lab 6 讲）。

> **Q：`JpaRepository` 继承链是什么？**
> A：`JpaRepository` → `PagingAndSortingRepository` → `CrudRepository` → `Repository`。每一层都增加了功能，`JpaRepository` 是最完整的。

> **Q：JPQL 和原生 SQL 有什么区别？**
> A：JPQL 是面向对象查询语言，使用 Java 类名和字段名，数据库无关（换数据库不用改）。原生 SQL 直接写 SQL，使用表名和列名，性能更可控但与数据库绑定。

> **Q：懒加载（LAZY）和急加载（EAGER）的区别，为什么推荐 LAZY？**
> A：EAGER 在查主表时立即查关联表（多一次 JOIN），LAZY 在访问关联字段时才查。推荐 LAZY 是因为不是所有场景都需要关联数据，EAGER 会产生不必要的查询，影响性能。

---

## 本章小结

你已经学会了：
- ✅ `@Entity`、`@Table`：声明 JPA 实体类
- ✅ `@Id`、`@GeneratedValue`：主键及生成策略
- ✅ `@Column`：字段约束（长度、非空、唯一等）
- ✅ `@Repository`、`JpaRepository`：数据访问层
- ✅ 方法命名规则：自动生成 SQL
- ✅ `@Query`：自定义 JPQL / 原生 SQL
- ✅ `@ManyToOne`、`@OneToMany`：关系映射

---

下一步 → [Lab 5: Service 层与事务](./Lab5-Service层与事务.md)
