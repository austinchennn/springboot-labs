# Lab 3: 请求参数与响应详解

## 学习目标
- 掌握 `@PathVariable`：获取 URL 路径中的变量
- 掌握 `@RequestParam`：获取查询参数（?key=value）
- 掌握 `@RequestBody`：获取请求体（JSON）
- 掌握 `ResponseEntity<T>`：精确控制响应状态码和响应头
- 了解 HTTP 状态码规范

---

## 注解 1：`@PathVariable` — 路径变量

### 是什么？
当 URL 中有动态部分时（比如 `/api/books/1` 中的 `1`），用 `@PathVariable` 提取这个值。

### 语法

```java
// URL 中用 {变量名} 占位
@GetMapping("/{id}")
public Book getBook(@PathVariable Long id) {
    // id 的值就是 URL 中 {id} 的值
    // 访问 /api/books/5 → id = 5
}

// 变量名不同时，需要指定绑定
@GetMapping("/{bookId}")
public Book getBook(@PathVariable("bookId") Long id) {
    // URL 里是 {bookId}，方法参数叫 id，需要显式指定
}

// 多个路径变量
@GetMapping("/{category}/{id}")
public Book getBook(@PathVariable String category, @PathVariable Long id) {
    // /api/books/tech/5 → category="tech", id=5
}
```

### 实际例子

```java
// 获取某分类下的书
@GetMapping("/category/{category}")
public List<Book> getByCategory(@PathVariable String category) {
    return bookList.stream()
            .filter(b -> category.equals(b.getCategory()))
            .toList();
}
```

---

## 注解 2：`@RequestParam` — 查询参数

### 是什么？
获取 URL 中 `?` 后面的参数（查询字符串）。
例如：`/api/books?keyword=Java&page=1&size=10`

### 语法

```java
// 基本用法
@GetMapping
public List<Book> searchBooks(@RequestParam String keyword) {
    // /api/books?keyword=Java → keyword = "Java"
}

// 设置默认值（当参数不传时使用默认值）
@GetMapping
public List<Book> searchBooks(
    @RequestParam(required = false) String keyword,        // 可以不传（null）
    @RequestParam(defaultValue = "1") int page,            // 默认第 1 页
    @RequestParam(defaultValue = "10") int size            // 默认每页 10 条
) {
    // ...
}

// 参数名不同时指定绑定
@GetMapping
public List<Book> searchBooks(@RequestParam("q") String query) {
    // /api/books?q=Java → query = "Java"
}
```

### `@PathVariable` vs `@RequestParam` 怎么选？

| 场景 | 使用 | 例子 |
|------|------|------|
| 标识某个**资源** | `@PathVariable` | `/books/1`（操作 id=1 的书） |
| **过滤/排序/分页** | `@RequestParam` | `/books?keyword=Java&page=2` |

---

## 注解 3：`@RequestBody` — 请求体

### 是什么？
把 HTTP 请求体中的 JSON 字符串，自动解析为 Java 对象。
POST/PUT 请求时传递数据用这个。

### 语法

```java
// 客户端发送：POST /api/books
// 请求体：{"title":"三体","author":"刘慈欣","price":45.00}
@PostMapping
public Book addBook(@RequestBody Book book) {
    // Spring 自动把 JSON → Book 对象
    // book.getTitle() == "三体"
}
```

### 底层原理
Spring 内置了 Jackson 库，自动完成 JSON ↔ Java 对象的转换（序列化/反序列化）。
字段名要匹配（JSON 的 key 和 Java 对象的字段名要一致）。

---

## `ResponseEntity<T>` — 精确控制响应

### 是什么？
默认情况下，方法返回对象，Spring 自动以 `200 OK` 响应。
`ResponseEntity` 让你**精确控制** HTTP 状态码、响应头、响应体。

### 为什么要用？

REST API 规范要求：
- 创建成功 → `201 Created`（不是 200）
- 找不到资源 → `404 Not Found`（不是 200 返回 null）
- 删除成功 → `204 No Content`（不是 200）

### 语法

```java
// 方式一：静态工厂方法（最常用）
ResponseEntity.ok(data)                      // 200 OK + data
ResponseEntity.ok().body(data)               // 200 OK + data
ResponseEntity.created(uri).body(data)       // 201 Created + Location 头 + data
ResponseEntity.noContent().build()           // 204 No Content
ResponseEntity.notFound().build()            // 404 Not Found
ResponseEntity.badRequest().body(error)      // 400 Bad Request + 错误信息

// 方式二：构造器（完全自定义）
new ResponseEntity<>(data, HttpStatus.OK)               // 200
new ResponseEntity<>(data, headers, HttpStatus.CREATED) // 201 + 自定义 header
```

### 泛型说明
`ResponseEntity<T>` 中的 `T` 是响应体的类型：
- `ResponseEntity<Book>` → 返回单个书籍
- `ResponseEntity<List<Book>>` → 返回书籍列表
- `ResponseEntity<String>` → 返回字符串消息
- `ResponseEntity<Void>` → 无响应体（如 204）

---

## 动手实践：升级 BookController

**修改 `BookController.java`，加入所有参数注解和 `ResponseEntity`：**

```java
package com.example.bookshelf.controller;

import com.example.bookshelf.entity.Book;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final List<Book> bookList = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public BookController() {
        // 初始化测试数据（和 Lab 2 相同）
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

        Book book3 = new Book();
        book3.setId(idCounter.getAndIncrement());
        book3.setTitle("三体");
        book3.setAuthor("刘慈欣");
        book3.setIsbn("978-7536692930");
        book3.setCategory("科幻");
        book3.setPrice(new BigDecimal("45.00"));
        book3.setStock(200);

        bookList.add(book1);
        bookList.add(book2);
        bookList.add(book3);
    }

    // ─────────────────────────────────────────────
    // GET /api/books
    // GET /api/books?keyword=Java
    // GET /api/books?keyword=Java&page=1&size=5
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) String keyword,          // 搜索关键词（可选）
            @RequestParam(defaultValue = "0") int page,              // 页码（默认第 0 页）
            @RequestParam(defaultValue = "10") int size              // 每页大小（默认 10）
    ) {
        List<Book> result = bookList.stream()
                // 如果有关键词，过滤书名或作者包含关键词的书
                .filter(book -> keyword == null || keyword.isBlank()
                        || book.getTitle().contains(keyword)
                        || book.getAuthor().contains(keyword))
                .collect(Collectors.toList());

        // 简单分页（真实项目用 JPA 的 Pageable，Lab 4 会讲）
        int start = page * size;
        int end = Math.min(start + size, result.size());
        if (start > result.size()) {
            return ResponseEntity.ok(List.of());  // 页码超出范围，返回空列表
        }

        return ResponseEntity.ok(result.subList(start, end));  // 200 OK
    }

    // ─────────────────────────────────────────────
    // GET /api/books/{id}
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookList.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)         // 找到了：200 OK + 书的数据
                .orElse(ResponseEntity.notFound().build());  // 找不到：404 Not Found
    }

    // ─────────────────────────────────────────────
    // GET /api/books/category/{category}
    // 例：/api/books/category/技术
    // ─────────────────────────────────────────────
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Book>> getByCategory(@PathVariable String category) {
        List<Book> result = bookList.stream()
                .filter(book -> category.equals(book.getCategory()))
                .toList();
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────
    // POST /api/books
    // 请求体：{"title":"...", "author":"...", ...}
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        book.setId(idCounter.getAndIncrement());
        bookList.add(book);

        // 创建成功应返回 201 Created（不是 200）
        // 并在 Location 响应头中告知新资源的 URL（REST 规范）
        URI location = URI.create("/api/books/" + book.getId());
        return ResponseEntity.created(location).body(book);  // 201 Created
    }

    // ─────────────────────────────────────────────
    // PUT /api/books/{id}
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestBody Book updatedBook
    ) {
        for (Book book : bookList) {
            if (book.getId().equals(id)) {
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setIsbn(updatedBook.getIsbn());
                book.setCategory(updatedBook.getCategory());
                book.setPrice(updatedBook.getPrice());
                book.setStock(updatedBook.getStock());
                return ResponseEntity.ok(book);  // 200 OK + 更新后的书
            }
        }
        return ResponseEntity.notFound().build();  // 404 Not Found
    }

    // ─────────────────────────────────────────────
    // DELETE /api/books/{id}
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        boolean removed = bookList.removeIf(book -> book.getId().equals(id));
        if (removed) {
            return ResponseEntity.noContent().build();  // 204 No Content（删除成功无响应体）
        }
        return ResponseEntity.notFound().build();       // 404 Not Found
    }
}
```

---

## 常用 HTTP 状态码

| 状态码 | 含义 | 使用场景 |
|--------|------|---------|
| 200 OK | 成功 | GET 查询成功、PUT 更新成功 |
| 201 Created | 创建成功 | POST 新增成功 |
| 204 No Content | 成功，无响应体 | DELETE 删除成功 |
| 400 Bad Request | 请求参数错误 | 参数校验失败 |
| 401 Unauthorized | 未认证 | 没有登录 |
| 403 Forbidden | 无权限 | 没有操作权限 |
| 404 Not Found | 资源不存在 | 查询的 ID 不存在 |
| 409 Conflict | 冲突 | ISBN 重复 |
| 500 Internal Server Error | 服务器内部错误 | 程序异常 |

---

## 测试示例

```bash
# 搜索包含"Java"的书
curl "http://localhost:8080/api/books?keyword=Java"

# 第 1 页，每页 2 本（从第 0 页开始）
curl "http://localhost:8080/api/books?page=0&size=2"

# 获取科幻类书籍
curl http://localhost:8080/api/books/category/科幻

# 获取不存在的书（会返回 404）
curl -v http://localhost:8080/api/books/999

# 新增书（响应会有 201 状态码和 Location 头）
curl -v -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"活着","author":"余华","isbn":"9787506365437","category":"文学","price":28.00,"stock":300}'
```

---

## 面试考点

> **Q：`@PathVariable` 和 `@RequestParam` 有什么区别，什么时候用哪个？**
> A：`@PathVariable` 从 URL 路径中提取变量（`/books/{id}`），用于标识资源。`@RequestParam` 从查询字符串中提取参数（`?keyword=Java`），用于过滤、排序、分页等。

> **Q：为什么 POST 要返回 201 而不是 200？**
> A：HTTP 规范中 201 表示"资源已创建"，同时 Location 响应头应指向新资源的 URL。这是 REST API 的标准规范，200 只表示请求处理成功，语义不如 201 准确。

> **Q：`ResponseEntity` 和直接返回对象有什么区别？**
> A：直接返回对象只能返回 200 状态码，无法设置响应头。`ResponseEntity` 可以精确控制状态码、响应头和响应体，符合 HTTP 规范要求。

> **Q：@RequestBody 是怎么把 JSON 转成 Java 对象的？**
> A：Spring 内置了 Jackson 库，通过 `HttpMessageConverter` 机制自动完成 JSON 字符串到 Java 对象的反序列化（字段名匹配），以及 Java 对象到 JSON 的序列化。

---

## 本章小结

你已经学会了：
- ✅ `@PathVariable`：从路径中提取变量
- ✅ `@RequestParam`：从查询字符串提取参数（支持默认值和可选参数）
- ✅ `@RequestBody`：把请求体 JSON 反序列化为 Java 对象
- ✅ `ResponseEntity<T>`：精确控制 HTTP 状态码和响应头
- ✅ HTTP 状态码规范

---

下一步 → [Lab 4: 数据持久化 JPA](./Lab4-数据持久化JPA.md)
