# Lab 7: 全局异常处理

## 学习目标
- 理解为什么要统一处理异常
- 自定义业务异常类
- 掌握 `@RestControllerAdvice`、`@ExceptionHandler`、`@ResponseStatus`
- 设计统一的 API 响应格式（大厂标准）

---

## 核心概念：为什么要统一异常处理？

**没有统一异常处理时，各种错误响应格式不一致：**
```json
// 校验失败：Spring 默认格式，很长，前端难解析
{"timestamp":"...","status":400,"error":"Bad Request","message":"...","path":"..."}

// 业务异常：我们自己抛的
{"message":"书籍不存在"}

// 未捕获异常：Spring Whitelabel 错误页
{"timestamp":"...","status":500,"error":"Internal Server Error"}
```

**统一后，所有响应格式一致，前端只需处理一种格式：**
```json
// 成功
{"code":200,"message":"操作成功","data":{...}}

// 失败
{"code":404,"message":"书籍 ID=99 不存在","data":null}

// 校验失败
{"code":400,"message":"参数校验失败","data":{"title":"书名不能为空","price":"价格不能为负数"}}
```

---

## 步骤 1：设计统一响应格式

**创建 `src/main/java/com/example/bookshelf/common/ApiResponse.java`：**

```java
package com.example.bookshelf.common;

// 统一 API 响应包装类
// 所有接口的返回值都包在这里面
public class ApiResponse<T> {

    private int code;        // HTTP 状态码
    private String message;  // 提示信息
    private T data;          // 实际数据（可以是任何类型）

    // 私有构造器（用静态工厂方法创建）
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // 创建成功（201）
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "创建成功", data);
    }

    // 失败响应
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // Getter（没有 Setter，不可变）
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
```

---

## 步骤 2：自定义业务异常

**创建 `src/main/java/com/example/bookshelf/exception/BookNotFoundException.java`：**

```java
package com.example.bookshelf.exception;

// 自定义异常：书籍不存在
// 继承 RuntimeException（不是 Checked Exception）
// 这样 @Transactional 会自动回滚
public class BookNotFoundException extends RuntimeException {

    private final Long bookId;

    public BookNotFoundException(Long bookId) {
        super("书籍不存在，ID: " + bookId);
        this.bookId = bookId;
    }

    public Long getBookId() {
        return bookId;
    }
}
```

**创建 `src/main/java/com/example/bookshelf/exception/DuplicateIsbnException.java`：**

```java
package com.example.bookshelf.exception;

// 自定义异常：ISBN 重复
public class DuplicateIsbnException extends RuntimeException {

    private final String isbn;

    public DuplicateIsbnException(String isbn) {
        super("ISBN 已存在: " + isbn);
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }
}
```

---

## 注解 1：`@RestControllerAdvice`

### 是什么？
`@RestControllerAdvice` 标记一个类为**全局异常处理器**，它会拦截所有 Controller 抛出的异常。

```java
@RestControllerAdvice
// 等价于：
@ControllerAdvice + @ResponseBody
```

- `@ControllerAdvice`：增强所有 Controller（可以做异常处理、数据绑定、Model 属性）
- `@RestControllerAdvice`：在此基础上，所有方法返回 JSON（而不是视图名）

---

## 注解 2：`@ExceptionHandler`

### 是什么？
标记一个方法，指定它处理某种（或某几种）异常类型。

```java
// 处理 BookNotFoundException 异常
@ExceptionHandler(BookNotFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleBookNotFound(BookNotFoundException ex) {
    return ResponseEntity.status(404).body(ApiResponse.error(404, ex.getMessage()));
}

// 处理多种异常（用数组）
@ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
    return ResponseEntity.badRequest().body(ApiResponse.error(400, ex.getMessage()));
}
```

---

## 注解 3：`@ResponseStatus`

### 是什么？
可以直接加在异常类或异常处理方法上，指定 HTTP 状态码，不需要手动设置 `ResponseEntity`。

```java
// 加在异常类上（简单用法）
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {
    // 抛出这个异常时，自动返回 404
}

// 加在处理方法上（和 @ExceptionHandler 配合）
@ExceptionHandler(BookNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public ApiResponse<Void> handleNotFound(BookNotFoundException ex) {
    return ApiResponse.error(404, ex.getMessage());
    // 自动返回 404 状态码，不需要 ResponseEntity
}
```

---

## 步骤 3：创建全局异常处理器

**创建 `src/main/java/com/example/bookshelf/exception/GlobalExceptionHandler.java`：**

```java
package com.example.bookshelf.exception;

import com.example.bookshelf.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice   // ← 全局异常处理器，拦截所有 Controller 的异常
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────
    // 处理自定义业务异常：书籍不存在（404）
    // ─────────────────────────────────────────────
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    // ─────────────────────────────────────────────
    // 处理自定义业务异常：ISBN 重复（409 Conflict）
    // ─────────────────────────────────────────────
    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateIsbn(DuplicateIsbnException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ex.getMessage()));
    }

    // ─────────────────────────────────────────────
    // 处理 @Valid 校验失败（400 Bad Request）
    // 抛出：MethodArgumentNotValidException
    // ─────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // 提取所有字段的错误信息
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 返回格式：
        // {"code":400,"message":"参数校验失败","data":{"title":"书名不能为空","price":"价格不能为负数"}}
        ApiResponse<Map<String, String>> response =
                new ApiResponse<>(400, "参数校验失败", errors) {
                    // 匿名类（因为 ApiResponse 构造器是私有的，这里临时绕过）
                };

        // 更好的做法：给 ApiResponse 加一个 package-private 构造器
        // 或者用 Builder 模式（Lab 9 用 Lombok @Builder 解决这个问题）
        return ResponseEntity.badRequest().body(response);
    }

    // ─────────────────────────────────────────────
    // 处理参数类型错误（例如传了字符串给需要 Long 的 @PathVariable）
    // ─────────────────────────────────────────────
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(Exception ex) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, "参数类型错误: " + ex.getMessage()));
    }

    // ─────────────────────────────────────────────
    // 兜底处理：所有未预期的异常（500 Internal Server Error）
    // ─────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        // 生产环境不要暴露内部错误信息！这里只是演示
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误，请联系管理员"));
    }
}
```

---

## 步骤 4：在 Service 中抛出自定义异常

**修改 `BookServiceImpl.java`：**

```java
@Override
@Transactional(readOnly = true)
public BookResponse findById(Long id) {
    return bookRepository.findById(id)
            .map(BookResponse::from)
            .orElseThrow(() -> new BookNotFoundException(id));  // ← 抛异常，不返回 null
}

@Override
@Transactional(rollbackFor = Exception.class)
public BookResponse create(BookRequest request) {
    // 检查 ISBN 是否已存在
    if (bookRepository.existsByIsbn(request.getIsbn())) {
        throw new DuplicateIsbnException(request.getIsbn());  // ← 抛异常
    }

    Book book = new Book();
    book.setTitle(request.getTitle());
    book.setAuthor(request.getAuthor());
    book.setIsbn(request.getIsbn());
    book.setCategory(request.getCategory());
    book.setPrice(request.getPrice());
    book.setStock(request.getStock());
    book.setPublishDate(request.getPublishDate());
    book.setCreatedAt(java.time.LocalDateTime.now());
    book.setUpdatedAt(java.time.LocalDateTime.now());

    return BookResponse.from(bookRepository.save(book));
}

@Override
@Transactional(rollbackFor = Exception.class)
public BookResponse update(Long id, BookRequest request) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));  // ← 抛异常

    book.setTitle(request.getTitle());
    book.setAuthor(request.getAuthor());
    book.setPrice(request.getPrice());
    book.setStock(request.getStock());
    book.setUpdatedAt(java.time.LocalDateTime.now());

    return BookResponse.from(bookRepository.save(book));
}
```

---

## 步骤 5：修改 Controller 使用 ApiResponse

**修改 `BookController.java`（所有返回改为 `ApiResponse<T>`）：**

```java
package com.example.bookshelf.controller;

import com.example.bookshelf.common.ApiResponse;
import com.example.bookshelf.dto.BookRequest;
import com.example.bookshelf.dto.BookResponse;
import com.example.bookshelf.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAllBooks() {
        List<BookResponse> books = bookService.findAll();
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Long id) {
        // 找不到会抛 BookNotFoundException，由 GlobalExceptionHandler 处理
        BookResponse book = bookService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> addBook(
            @Valid @RequestBody BookRequest request) {
        // 校验失败抛 MethodArgumentNotValidException，由 GlobalExceptionHandler 处理
        // ISBN 重复抛 DuplicateIsbnException，由 GlobalExceptionHandler 处理
        BookResponse saved = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        BookResponse updated = bookService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

---

## 测试效果

```bash
# 查询不存在的书（404）
curl http://localhost:8080/api/books/999
# 响应：{"code":404,"message":"书籍不存在，ID: 999","data":null}

# 提交无效数据（400）
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"","price":-1}'
# 响应：{"code":400,"message":"参数校验失败","data":{"title":"书名不能为空","price":"价格最低 0.01 元","author":"作者不能为空","isbn":"ISBN 不能为空","stock":"库存不能为空"}}

# 正常查询（200）
curl http://localhost:8080/api/books/1
# 响应：{"code":200,"message":"操作成功","data":{"id":1,"title":"Java 编程思想",...}}
```

---

## 面试考点

> **Q：@ControllerAdvice 和 @RestControllerAdvice 的区别？**
> A：`@RestControllerAdvice = @ControllerAdvice + @ResponseBody`，方法返回值自动序列化为 JSON。`@ControllerAdvice` 可以返回视图名，适合传统 MVC。做 REST API 用 `@RestControllerAdvice`。

> **Q：@ExceptionHandler 中，如果多个方法都能处理同一个异常，哪个生效？**
> A：精确匹配优先。异常层级越具体（子类）的处理方法优先级越高。最后兜底的 `Exception.class` 处理所有未被处理的异常。

> **Q：全局异常处理的执行顺序？**
> A：Controller 抛出异常 → Spring 在 `@RestControllerAdvice` 类中找匹配的 `@ExceptionHandler` → 找到则执行，没找到则继续向上（找父类异常的处理器）→ 最终找 `Exception.class` 的处理器。

> **Q：自定义异常为什么要继承 RuntimeException 而不是 Exception？**
> A：`RuntimeException` 是 Unchecked Exception，不需要在方法签名上声明 `throws`，代码更简洁。同时 `@Transactional` 默认只在 `RuntimeException` 时回滚，继承它能确保事务正确回滚。

---

## 本章小结

你已经学会了：
- ✅ 统一响应格式 `ApiResponse<T>`
- ✅ 自定义业务异常（继承 `RuntimeException`）
- ✅ `@RestControllerAdvice`：全局异常处理器
- ✅ `@ExceptionHandler`：处理特定异常
- ✅ `@ResponseStatus`：指定响应状态码
- ✅ 处理 `MethodArgumentNotValidException`（校验失败）

---

下一步 → [Lab 8: 配置管理](./Lab8-配置管理.md)
