# Lab 2: 第一个 REST API

## 学习目标
- 理解 REST 风格 API 的设计规范
- 掌握 `@RestController` 和 `@Controller` 的区别
- 掌握 `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`
- 写出第一个完整的书籍 CRUD API（先用内存存储，不连数据库）

---

## 核心概念：什么是 REST API？

REST 是一种 API 设计风格，核心规范：

| HTTP 方法 | 操作 | 例子 |
|-----------|------|------|
| GET | 查询 | 获取所有书籍、获取单本书 |
| POST | 创建 | 新增一本书 |
| PUT | 全量更新 | 修改整本书的信息 |
| PATCH | 部分更新 | 只修改书的价格 |
| DELETE | 删除 | 删除一本书 |

**URL 设计规范：**
```
GET    /api/books          → 查询所有书
GET    /api/books/{id}     → 查询 id=1 的书
POST   /api/books          → 新增一本书
PUT    /api/books/{id}     → 修改 id=1 的书（全量）
DELETE /api/books/{id}     → 删除 id=1 的书
```

**规范要点：**
- URL 用名词（`/books`），不用动词（~~`/getBooks`~~）
- 用复数（`/books`），不用单数（~~`/book`~~）
- 用 HTTP 方法区分操作，不要在 URL 里写 `/deleteBook`

---

## 注解 1：`@RestController`

### 是什么？
`@RestController` 标记一个类是 **REST API 控制器**，所有方法的返回值都会自动转成 JSON 返回给客户端。

它是两个注解的组合：
```java
@RestController
// 等价于：
@Controller        // 表示这是控制器（继承自 @Component，会被 Spring 管理）
@ResponseBody      // 所有方法返回值都序列化为 JSON（而不是跳转到视图）
```

### `@Controller` vs `@RestController`

| | `@Controller` | `@RestController` |
|--|--------------|------------------|
| 用途 | 传统 MVC，返回页面视图 | REST API，返回 JSON |
| 需要加 `@ResponseBody` | 每个方法都要加 | 不需要，已内置 |
| 现在还用吗 | 渲染 Thymeleaf/JSP 时用 | **后端 API 开发全用这个** |

大厂后端基本都是前后端分离，所以**你几乎只会用 `@RestController`**。

---

## 注解 2：`@RequestMapping`

### 是什么？
`@RequestMapping` 把 URL 路径映射到控制器或方法上。可以加在**类**上（公共前缀）也可以加在**方法**上（具体路径）。

```java
@RestController
@RequestMapping("/api/books")    // 类级别：所有方法的 URL 前缀
public class BookController {

    @RequestMapping(method = RequestMethod.GET)    // 方法级别：GET /api/books
    public List<Book> getAllBooks() { ... }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)  // GET /api/books/{id}
    public Book getBook(@PathVariable Long id) { ... }
}
```

因为每次都要写 `method = RequestMethod.GET` 太繁琐了，Spring 提供了快捷注解：

---

## 注解 3：`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping`

这五个注解都是 `@RequestMapping` 的快捷方式：

```java
@GetMapping("/path")    等同于  @RequestMapping(value="/path", method=RequestMethod.GET)
@PostMapping("/path")   等同于  @RequestMapping(value="/path", method=RequestMethod.POST)
@PutMapping("/path")    等同于  @RequestMapping(value="/path", method=RequestMethod.PUT)
@DeleteMapping("/path") 等同于  @RequestMapping(value="/path", method=RequestMethod.DELETE)
@PatchMapping("/path")  等同于  @RequestMapping(value="/path", method=RequestMethod.PATCH)
```

**实际开发中，除了类级别用 `@RequestMapping` 定义前缀，方法级别全用这五个。**

---

## 动手实践：构建完整的书籍 CRUD API

### 步骤 1：创建 Book 实体类（临时版本，Lab 4 会升级）

**创建文件：`src/main/java/com/example/bookshelf/entity/Book.java`**
```java
package com.example.bookshelf.entity;

import java.math.BigDecimal;

// 这是一个普通 Java 类（POJO），Lab 4 会加上 JPA 注解让它映射到数据库
public class Book {

    private Long id;
    private String title;    // 书名
    private String author;   // 作者
    private String isbn;     // ISBN 号
    private String category; // 分类
    private BigDecimal price; // 价格
    private Integer stock;   // 库存

    // 手写 getter/setter（Lab 9 会用 Lombok 一行代替这些）
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
}
```

### 步骤 2：创建 BookController

**创建文件：`src/main/java/com/example/bookshelf/controller/BookController.java`**
```java
package com.example.bookshelf.controller;

import com.example.bookshelf.entity.Book;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController                      // ← 这个类是 REST 控制器，返回值自动转 JSON
@RequestMapping("/api/books")        // ← 这个类所有方法的 URL 前缀是 /api/books
public class BookController {

    // 临时内存存储（List 模拟数据库，Lab 4 会换成真实数据库）
    private final List<Book> bookList = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1); // 自增 ID

    // 初始化一些测试数据
    public BookController() {
        Book book1 = new Book();
        book1.setId(idCounter.getAndIncrement());
        book1.setTitle("Java 编程思想");
        book1.setAuthor("Bruce Eckel");
        book1.setIsbn("978-0131872494");
        book1.setCategory("技术");
        book1.setPrice(new BigDecimal("89.00"));
        book1.setStock(100);

        Book book2 = new Book();
        book2.setId(idCounter.getAndIncrement());
        book2.setTitle("深入理解 Java 虚拟机");
        book2.setAuthor("周志明");
        book2.setIsbn("978-7111641247");
        book2.setCategory("技术");
        book2.setPrice(new BigDecimal("129.00"));
        book2.setStock(50);

        bookList.add(book1);
        bookList.add(book2);
    }

    // ─────────────────────────────────────────────
    // GET /api/books    ← 查询所有书
    // ─────────────────────────────────────────────
    @GetMapping                  // 等价于 @RequestMapping(method = RequestMethod.GET)
    public List<Book> getAllBooks() {
        return bookList;         // Spring 自动把 List<Book> 转成 JSON 数组
    }

    // ─────────────────────────────────────────────
    // GET /api/books/1  ← 查询 id=1 的书
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")         // {id} 是路径变量，下一个 Lab 详细讲
    public Book getBookById(@PathVariable Long id) {
        return bookList.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);  // 找不到返回 null（Lab 7 会改成抛异常）
    }

    // ─────────────────────────────────────────────
    // POST /api/books   ← 新增一本书
    // 请求体（JSON）：{"title":"xx","author":"xx",...}
    // ─────────────────────────────────────────────
    @PostMapping
    public Book addBook(@RequestBody Book book) {  // @RequestBody：把请求的 JSON 转成对象
        book.setId(idCounter.getAndIncrement());   // 分配 ID
        bookList.add(book);
        return book;            // 返回新增的书（含 ID）
    }

    // ─────────────────────────────────────────────
    // PUT /api/books/1  ← 全量更新 id=1 的书
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        for (Book book : bookList) {
            if (book.getId().equals(id)) {
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setIsbn(updatedBook.getIsbn());
                book.setCategory(updatedBook.getCategory());
                book.setPrice(updatedBook.getPrice());
                book.setStock(updatedBook.getStock());
                return book;
            }
        }
        return null;            // 找不到（Lab 7 会改成抛异常）
    }

    // ─────────────────────────────────────────────
    // DELETE /api/books/1  ← 删除 id=1 的书
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookList.removeIf(book -> book.getId().equals(id));
        return "删除成功";
    }
}
```

---

## 步骤 3：测试 API

运行程序后，用以下方式测试：

### 方法一：用 curl 命令测试

```bash
# 查询所有书
curl http://localhost:8080/api/books

# 查询 id=1 的书
curl http://localhost:8080/api/books/1

# 新增一本书（-H 设置请求头，-d 设置请求体）
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"三体","author":"刘慈欣","isbn":"9787536692930","category":"科幻","price":45.00,"stock":200}'

# 修改书籍
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Java 编程思想（第4版）","author":"Bruce Eckel","isbn":"978-0131872494","category":"技术","price":99.00,"stock":80}'

# 删除书籍
curl -X DELETE http://localhost:8080/api/books/1
```

### 方法二：安装 Postman（推荐！）

Postman 是 API 测试工具，提供图形界面，比 curl 直观很多。
下载地址：https://www.postman.com/downloads/

Postman 基本使用：
1. 新建请求
2. 选择方法（GET/POST/PUT/DELETE）
3. 输入 URL
4. POST/PUT 时在 Body 里选 raw → JSON，输入请求体
5. 点 Send

---

## 注解关系总结图

```
@SpringBootApplication
    └── @ComponentScan  →  扫描到  →  @RestController（继承自 @Component）

@RestController 标记的类：
    ├── @GetMapping     → 处理 GET 请求
    ├── @PostMapping    → 处理 POST 请求
    ├── @PutMapping     → 处理 PUT 请求
    ├── @DeleteMapping  → 处理 DELETE 请求
    └── @PatchMapping   → 处理 PATCH 请求
```

---

## 面试考点

> **Q：`@Controller` 和 `@RestController` 的区别？**
> A：`@RestController = @Controller + @ResponseBody`。`@Controller` 用于传统 MVC 返回视图；`@RestController` 用于 REST API，所有方法的返回值自动序列化为 JSON。

> **Q：`@RequestMapping` 和 `@GetMapping` 的关系？**
> A：`@GetMapping` 是 `@RequestMapping(method=RequestMethod.GET)` 的快捷注解，更简洁。实际开发中，类级别用 `@RequestMapping` 定义 URL 前缀，方法级别用具体的 `@GetMapping` 等。

> **Q：REST API 设计规范说几个要点？**
> A：URL 用名词不用动词，用复数（/books），用 HTTP 方法区分 CRUD 操作，用 HTTP 状态码表示结果（200/201/404/500 等）。

---

## 本章小结

你已经学会了：
- ✅ `@RestController`：REST API 控制器
- ✅ `@RequestMapping`：URL 路径映射（类级别）
- ✅ `@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`：方法级别快捷注解
- ✅ REST API 设计规范
- ✅ 完整的 CRUD API

---

下一步 → [Lab 3: 请求与响应详解](./Lab3-请求与响应.md)
