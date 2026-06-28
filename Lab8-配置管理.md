# Lab 8: 配置管理

## 学习目标
- 掌握 `@Configuration` 和 `@Bean` 的完整用法
- 掌握 `@ConfigurationProperties`（比 `@Value` 更强的配置绑定）
- 掌握 `@Profile`（多环境配置）
- 理解 `@ConditionalOnProperty`（条件装配）
- 搭建开发/生产双环境配置

---

## 注解 1：`@Configuration`

### 是什么？
标记一个类是**配置类**，里面可以定义 `@Bean`。
`@Configuration` 本质上也是 `@Component`（所以会被 Spring 扫描到）。

```java
@Configuration
public class AppConfig {

    @Bean
    public SomeThirdPartyService someService() {
        return new SomeThirdPartyService("初始化参数");
    }
}
```

### `@Configuration` vs 普通 `@Component` + `@Bean` 的区别

这是一个**高级但重要的细节**：

```java
// 方式 1：@Configuration（推荐）
@Configuration
public class Config1 {
    @Bean
    public A beanA() { return new A(); }

    @Bean
    public B beanB() {
        return new B(beanA());  // ← 调用 beanA()
        // Spring 会返回已经创建的 beanA 单例！不会重新 new A()
        // 因为 @Configuration 的类被 CGLIB 代理了
    }
}

// 方式 2：@Component（lite 模式）
@Component
public class Config2 {
    @Bean
    public A beanA() { return new A(); }

    @Bean
    public B beanB() {
        return new B(beanA());  // ← 调用 beanA()
        // 这里会 new 一个新的 A()！不是单例！
        // @Component 不经过 CGLIB 代理
    }
}
```

**结论：配置类要用 `@Configuration`，不要用 `@Component`！**

---

## 注解 2：`@ConfigurationProperties` — 批量绑定配置

### 为什么不用 `@Value`？

如果配置项很多：
```java
// 用 @Value 一个个写，很麻烦
@Value("${app.mail.host}")
private String mailHost;
@Value("${app.mail.port}")
private int mailPort;
@Value("${app.mail.username}")
private String mailUsername;
// ... 还有 10 个字段
```

**用 `@ConfigurationProperties` 一次绑定整个配置前缀，优雅得多：**

### 步骤 1：在 `application.yml` 添加配置

```yaml
# application.yml（添加以下内容）
app:
  bookshelf:
    # 业务配置
    max-books-per-page: 20        # 每页最多显示书籍数
    default-category: 综合         # 默认分类
    low-stock-threshold: 10        # 低库存阈值
    # 功能开关
    cache-enabled: true
    audit-log-enabled: true
```

### 步骤 2：创建配置属性类

**创建 `src/main/java/com/example/bookshelf/config/BookshelfProperties.java`：**

```java
package com.example.bookshelf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// prefix：yml 中的前缀，Spring 自动把该前缀下的配置绑定到这个类的字段
// 字段名自动对应 yml 中的 key（驼峰 ↔ 连字符自动转换）
// maxBooksPerPage ↔ max-books-per-page（Spring Boot 的 Relaxed Binding）
@Component
@ConfigurationProperties(prefix = "app.bookshelf")
public class BookshelfProperties {

    private int maxBooksPerPage = 10;   // 默认值
    private String defaultCategory = "综合";
    private int lowStockThreshold = 5;
    private boolean cacheEnabled = false;
    private boolean auditLogEnabled = false;

    // 必须有 Getter/Setter（Spring 通过 Setter 注入配置值）
    public int getMaxBooksPerPage() { return maxBooksPerPage; }
    public void setMaxBooksPerPage(int maxBooksPerPage) { this.maxBooksPerPage = maxBooksPerPage; }
    public String getDefaultCategory() { return defaultCategory; }
    public void setDefaultCategory(String defaultCategory) { this.defaultCategory = defaultCategory; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }
    public boolean isAuditLogEnabled() { return auditLogEnabled; }
    public void setAuditLogEnabled(boolean auditLogEnabled) { this.auditLogEnabled = auditLogEnabled; }
}
```

### 步骤 3：使用配置属性

```java
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookshelfProperties properties;  // ← 注入配置

    public BookServiceImpl(BookRepository bookRepository,
                           BookshelfProperties properties) {
        this.bookRepository = bookRepository;
        this.properties = properties;
    }

    public List<BookResponse> findLowStockBooks() {
        // 使用配置中的低库存阈值（而不是硬编码 10）
        return bookRepository.findLowStockBooks(properties.getLowStockThreshold())
                .stream()
                .map(BookResponse::from)
                .toList();
    }
}
```

### `@ConfigurationProperties` 还可以绑定复杂对象

```yaml
app:
  mail:
    host: smtp.example.com
    port: 587
    credentials:
      username: admin@example.com
      password: secret
    recipients:
      - admin@example.com
      - dev@example.com
```

```java
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private String host;
    private int port;
    private Credentials credentials;     // ← 嵌套对象
    private List<String> recipients;     // ← 列表

    // 嵌套类
    public static class Credentials {
        private String username;
        private String password;
        // getter/setter...
    }

    // getter/setter...
}
```

---

## 注解 3：`@Profile` — 多环境配置

### 什么是 Profile？
大公司通常有多套环境：
- **dev**（开发）：H2 内存数据库，日志详细，缓存关闭
- **test**（测试）：MySQL 测试库，日志中等
- **prod**（生产）：MySQL 主库，日志精简，缓存开启

`@Profile` 让某些 Bean 或配置**只在特定环境下生效**。

### 方法一：多配置文件（最常用）

**创建 `src/main/resources/application-dev.yml`（开发环境）：**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:bookshelfdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop    # 开发：每次重启重建表
    show-sql: true              # 开发：打印 SQL

logging:
  level:
    com.example.bookshelf: DEBUG   # 开发：详细日志
    org.hibernate.SQL: DEBUG

app:
  bookshelf:
    cache-enabled: false           # 开发：关闭缓存，方便调试
    audit-log-enabled: true
```

**创建 `src/main/resources/application-prod.yml`（生产环境）：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db-server:3306/bookshelf
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}       # 从环境变量读取（不硬编码密码！）
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20        # 生产：连接池大
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate           # 生产：只验证表结构，不自动建表！
    show-sql: false                 # 生产：关闭 SQL 打印（性能）

logging:
  level:
    root: WARN                      # 生产：只打印警告和错误
    com.example.bookshelf: INFO

app:
  bookshelf:
    cache-enabled: true             # 生产：开启缓存
    audit-log-enabled: true
```

**激活 Profile（两种方式）：**

```yaml
# application.yml 中设置（开发时用）
spring:
  profiles:
    active: dev   # 激活 dev profile
```

```bash
# 启动时通过命令行参数设置（生产部署用）
java -jar bookshelf.jar --spring.profiles.active=prod

# 或通过环境变量
SPRING_PROFILES_ACTIVE=prod java -jar bookshelf.jar
```

### 方法二：`@Profile` 注解条件 Bean

```java
// 只在 dev 和 test 环境下加载（生产不加载）
@Configuration
@Profile({"dev", "test"})
public class TestDataConfig {

    @Bean
    public CommandLineRunner initTestData(BookRepository bookRepository) {
        return args -> {
            // 初始化测试数据（只在 dev/test 环境运行）
            bookRepository.save(createSampleBook("Java 编程思想", "Bruce Eckel"));
            bookRepository.save(createSampleBook("三体", "刘慈欣"));
            System.out.println("✅ 测试数据已加载");
        };
    }

    private Book createSampleBook(String title, String author) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn("000-" + System.currentTimeMillis());
        book.setPrice(new java.math.BigDecimal("49.00"));
        book.setStock(100);
        book.setCreatedAt(java.time.LocalDateTime.now());
        book.setUpdatedAt(java.time.LocalDateTime.now());
        return book;
    }
}
```

```java
// 只在生产环境使用真实的邮件服务
@Service
@Profile("prod")
public class RealEmailService implements EmailService {
    // 发送真实邮件
}

// 开发/测试环境用假邮件服务（不真正发送）
@Service
@Profile({"dev", "test"})
public class MockEmailService implements EmailService {
    @Override
    public void send(String to, String content) {
        System.out.println("[MOCK EMAIL] 发送给: " + to + " 内容: " + content);
        // 不真正发送邮件
    }
}
```

---

## 注解 4：`@ConditionalOnProperty` — 按配置条件装配

```java
// 只有当 app.bookshelf.cache-enabled=true 时，才创建这个 Bean
@Bean
@ConditionalOnProperty(
    name = "app.bookshelf.cache-enabled",
    havingValue = "true",
    matchIfMissing = false   // 如果配置不存在，不装配（默认行为）
)
public CacheConfig cacheConfig() {
    System.out.println("✅ 缓存已开启");
    return new CacheConfig();
}
```

**常用 `@Conditional` 系列注解：**

| 注解 | 条件 |
|------|------|
| `@ConditionalOnProperty` | 配置属性满足条件时 |
| `@ConditionalOnClass` | classpath 中存在某个类时（如某依赖已加载） |
| `@ConditionalOnMissingBean` | 容器中不存在某个 Bean 时（可覆盖默认实现） |
| `@ConditionalOnBean` | 容器中存在某个 Bean 时 |
| `@ConditionalOnExpression` | SpEL 表达式为 true 时 |

---

## 完整 `application.yml` 总览

```yaml
# application.yml（公共配置）
spring:
  application:
    name: bookshelf
  profiles:
    active: dev   # 默认开发环境（部署时通过命令行覆盖）

server:
  port: 8080
  servlet:
    context-path: /           # API 根路径（可改为 /api 变成 /api/books）

# 自定义业务配置
app:
  bookshelf:
    max-books-per-page: 20
    default-category: 综合
    low-stock-threshold: 10
    cache-enabled: false
    audit-log-enabled: false
```

---

## 面试考点

> **Q：@Value 和 @ConfigurationProperties 的区别，什么时候用哪个？**
> A：`@Value` 用于注入单个配置值（如 `@Value("${server.port}")`）。`@ConfigurationProperties` 用于绑定一组有相同前缀的配置到一个类，支持类型安全、IDE 自动补全和配置验证。有多个相关配置时优先用 `@ConfigurationProperties`。

> **Q：Spring Boot 的配置文件优先级？**
> A：优先级从高到低：命令行参数 > 环境变量 > `application-{profile}.yml` > `application.yml` > 默认值。高优先级会覆盖低优先级的同名配置。

> **Q：@Profile 的使用场景？**
> A：多环境配置（dev/test/prod），比如不同环境用不同数据库、不同第三方服务（真实 vs Mock）、不同日志级别。还可以用于只在特定环境执行的数据初始化。

---

## 本章小结

你已经学会了：
- ✅ `@Configuration`：定义配置类（不要用 `@Component` 替代）
- ✅ `@ConfigurationProperties`：批量绑定 yml 配置到 Java 类
- ✅ `@Profile`：多环境配置（dev/prod 分开）
- ✅ `@ConditionalOnProperty`：按配置条件装配 Bean
- ✅ 生产安全：密码从环境变量读取，不硬编码

---

下一步 → [Lab 9: Lombok 提效工具](./Lab9-Lombok.md)
