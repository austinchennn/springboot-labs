# Lab 6: 数据校验 — @Valid 与 Bean Validation

## 学习目标
- 理解为什么要在 Service 层之前校验数据
- 掌握 `@Valid` 和 `@Validated` 的用法和区别
- 掌握所有常用校验注解
- 学会处理校验失败的响应

---

## 核心概念：为什么要数据校验？

**没有校验会发生什么：**
```java
// 前端发来的请求体：{"title":"","price":-100,"stock":null}
bookService.create(request);
// 书名为空、价格为负数、库存为 null 都存进数据库了！
```

**数据校验的原则：**
- **在系统边界处校验**（Controller 层，数据进来的第一关）
- **快速失败**：校验不通过立即返回 400，不进入业务逻辑
- **错误信息友好**：告诉前端哪个字段有什么问题

Spring 集成了 **Bean Validation（JSR-303/JSR-380）** 规范，使用 Hibernate Validator 实现。

---

## 注解 1：`@Valid` — 触发校验

`@Valid` 加在 Controller 方法的参数前，触发对该参数的校验（参数上的约束注解会生效）。

```java
@PostMapping
public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookRequest request) {
    //                                       ↑ 加了 @Valid，Spring 会自动校验 request
    //                                         如果校验失败，直接返回 400
    return ResponseEntity.ok(bookService.create(request));
}
```

---

## 常用约束注解（加在 DTO 字段上）

### 空值约束

```java
// @NotNull：不允许 null（但允许空字符串 ""）
@NotNull(message = "价格不能为空")
private BigDecimal price;

// @NotBlank：不允许 null、空字符串、纯空格（最常用于字符串）
@NotBlank(message = "书名不能为空")
private String title;

// @NotEmpty：不允许 null 和空（长度 > 0），适用于字符串、集合、数组
@NotEmpty(message = "分类不能为空")
private String category;
```

**三者区别：**

| 注解 | null | `""` | `" "` | 适用 |
|------|------|------|-------|------|
| `@NotNull` | ✗ | ✓ | ✓ | 任何类型 |
| `@NotEmpty` | ✗ | ✗ | ✓ | String、集合 |
| `@NotBlank` | ✗ | ✗ | ✗ | **String（最常用）** |

### 长度约束

```java
// @Size：String 长度约束，或集合大小约束
@Size(min = 1, max = 200, message = "书名长度必须在 1-200 字符之间")
private String title;

@Size(max = 20, message = "ISBN 不超过 20 位")
private String isbn;

// @Length：Hibernate 扩展，功能同 @Size（常用）
@Length(max = 100, message = "作者名不超过 100 字符")
private String author;
```

### 数值约束

```java
// @Min：最小值
@Min(value = 0, message = "价格不能为负数")
private BigDecimal price;

// @Max：最大值
@Max(value = 9999, message = "库存最大 9999")
private Integer stock;

// @Positive：必须是正数（> 0）
@Positive(message = "价格必须大于 0")
private BigDecimal price;

// @PositiveOrZero：大于等于 0
@PositiveOrZero(message = "库存不能为负数")
private Integer stock;

// @Negative：必须是负数（< 0）—— 很少用
// @NegativeOrZero：小于等于 0 —— 很少用

// @DecimalMin / @DecimalMax：小数约束（更精确）
@DecimalMin(value = "0.01", message = "价格最低 0.01 元")
@DecimalMax(value = "99999.99", message = "价格最高 99999.99 元")
private BigDecimal price;
```

### 格式约束

```java
// @Email：邮箱格式
@Email(message = "邮箱格式不正确")
private String email;

// @Pattern：正则表达式（最灵活）
@Pattern(regexp = "^978-\\d{10}$", message = "ISBN 格式必须是 978-XXXXXXXXXX")
private String isbn;

// @URL：必须是合法 URL
@URL(message = "封面必须是合法 URL")
private String coverUrl;
```

### 日期约束

```java
// @Past：必须是过去的日期
@Past(message = "出版日期必须是过去的日期")
private LocalDate publishDate;

// @Future：必须是未来的日期
@Future(message = "预约时间必须是未来")
private LocalDateTime appointmentTime;

// @PastOrPresent：过去或现在
// @FutureOrPresent：现在或未来
```

---

## 动手实践：给 BookRequest 加校验

**修改 `src/main/java/com/example/bookshelf/dto/BookRequest.java`：**

```java
package com.example.bookshelf.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookRequest {

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名最长 200 字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 100, message = "作者名最长 100 字符")
    private String author;

    @NotBlank(message = "ISBN 不能为空")
    @Pattern(regexp = "^\\d{3}-\\d{10}$", message = "ISBN 格式不正确，应为 XXX-XXXXXXXXXX")
    private String isbn;

    @Size(max = 50, message = "分类名最长 50 字符")
    private String category;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格最低 0.01 元")
    @DecimalMax(value = "99999.99", message = "价格最高 99999.99 元")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 99999, message = "库存最大 99999")
    private Integer stock;

    @Past(message = "出版日期必须是过去的日期")
    private LocalDate publishDate;

    // Getter/Setter
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
}
```

**修改 BookController，加上 `@Valid`：**

```java
@PostMapping
public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookRequest request) {
    //                                        ↑ 加了 @Valid，校验失败自动返回 400
    BookResponse saved = bookService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

@PutMapping("/{id}")
public ResponseEntity<BookResponse> updateBook(
        @PathVariable Long id,
        @Valid @RequestBody BookRequest request) {   // ← 这里也加
    BookResponse updated = bookService.update(id, request);
    return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
}
```

---

## 校验失败时的默认响应（很丑，需要自定义）

发送一个无效请求：
```json
{"title":"","price":-1,"stock":null}
```

默认响应（Spring 自动生成，很长很丑）：
```json
{
  "timestamp": "2024-01-01T00:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for object='bookRequest'. Error count: 3",
  "path": "/api/books"
}
```

**我们需要自定义错误响应，Lab 7 会处理这个。**

---

## `@Valid` vs `@Validated`

| | `@Valid` | `@Validated` |
|--|---------|------------|
| 来源 | Jakarta EE 标准（JSR-303） | Spring 框架 |
| 用于 Controller | ✅ | ✅ |
| 支持分组校验 | ✗ | ✅ |
| 用于 Service 方法参数 | ✗ | ✅ |

**分组校验（`@Validated` 的高级用法）：**

```java
// 定义校验分组
public interface CreateGroup {}
public interface UpdateGroup {}

public class BookRequest {
    // 创建时 ISBN 必填，更新时可以不填（ISBN 一般不改）
    @NotBlank(message = "ISBN 不能为空", groups = CreateGroup.class)
    private String isbn;

    // 更新时 ID 必填，创建时不填（创建时 ID 由数据库生成）
    @NotNull(message = "ID 不能为空", groups = UpdateGroup.class)
    private Long id;
}

// Controller 中使用分组
@PostMapping
public ResponseEntity<BookResponse> addBook(
    @Validated(CreateGroup.class) @RequestBody BookRequest request) { ... }

@PutMapping("/{id}")
public ResponseEntity<BookResponse> updateBook(
    @PathVariable Long id,
    @Validated(UpdateGroup.class) @RequestBody BookRequest request) { ... }
```

---

## 自定义校验注解（高级）

当内置注解不够用时，可以自定义：

```java
// 1. 定义注解
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsbnValidator.class)
public @interface ValidIsbn {
    String message() default "ISBN 格式无效";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 2. 实现校验逻辑
public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {
    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null) return true;  // null 交给 @NotNull 处理
        // 这里写你的校验逻辑，例如验证 ISBN-13 的校验位
        return isbn.matches("^978-\\d{10}$");
    }
}

// 3. 使用自定义注解
public class BookRequest {
    @ValidIsbn
    private String isbn;
}
```

---

## 面试考点

> **Q：@NotNull、@NotEmpty、@NotBlank 有什么区别？**
> A：`@NotNull` 只检查不为 null，空字符串通过。`@NotEmpty` 检查不为 null 且长度>0，纯空格通过。`@NotBlank` 检查不为 null、不为空字符串、不为纯空格，最严格，最常用于字符串。

> **Q：@Valid 和 @Validated 有什么区别？**
> A：`@Valid` 是 Java EE 标准注解，`@Validated` 是 Spring 扩展。主要区别：`@Validated` 支持**分组校验**（不同场景校验不同字段），而 `@Valid` 不支持。`@Validated` 还可以用在 Service 层方法参数上。

> **Q：校验失败会抛什么异常？**
> A：Controller 接收 `@RequestBody` 校验失败抛 `MethodArgumentNotValidException`；`@PathVariable`/`@RequestParam` 校验失败（配合 `@Validated`）抛 `ConstraintViolationException`。Lab 7 的全局异常处理会捕获这两种。

---

## 本章小结

你已经学会了：
- ✅ `@Valid`：触发 Controller 参数校验
- ✅ 空值约束：`@NotNull`、`@NotEmpty`、`@NotBlank`
- ✅ 长度约束：`@Size`
- ✅ 数值约束：`@Min`、`@Max`、`@Positive`、`@DecimalMin`/`@DecimalMax`
- ✅ 格式约束：`@Email`、`@Pattern`、`@URL`
- ✅ 日期约束：`@Past`、`@Future`
- ✅ `@Valid` vs `@Validated`（分组校验）

---

下一步 → [Lab 7: 全局异常处理](./Lab7-异常处理.md)
