# Lab 1: Spring Boot 核心注解

## 学习目标
- 理解 Spring 最核心的机制：IoC 容器
- 掌握 `@SpringBootApplication` 做了什么
- 掌握组件类注解：`@Component`、`@Service`、`@Repository`、`@Controller`
- 掌握 `@Bean`、`@Autowired`、`@Value`
- 理解构造器注入（企业推荐写法）

---

## 核心概念：IoC 容器（必须先懂！）

**普通 Java 代码的问题：**
```java
// 每次需要对象都要自己 new
BookService bookService = new BookServiceImpl();
BookController controller = new BookController(bookService);
```
这样做的问题：对象由**你自己**管理，类之间**强耦合**（BookController 必须知道 BookServiceImpl 的具体实现）。

**Spring 的解决方案：IoC（控制反转）**
- 你告诉 Spring "这个类需要被管理"（用注解标记）
- Spring 自动创建对象、管理生命周期
- 需要用的时候，Spring 自动注入给你
- 这些被 Spring 管理的对象叫做 **Bean**

```
你的代码  →  告诉 Spring "我要被管理"  →  Spring 容器（存放所有 Bean）
你的代码  →  告诉 Spring "我需要这个"  →  Spring 自动注入
```

---

## 注解 1：`@SpringBootApplication`

### 是什么？
这是 Spring Boot 项目**必须有**的注解，标在启动类上。它是三个注解的组合：

```java
@SpringBootApplication
// 等价于：
@SpringBootConfiguration   // 等同于 @Configuration，表示这是配置类
@EnableAutoConfiguration   // 开启自动配置（Spring Boot 最强大的特性！）
@ComponentScan             // 扫描当前包及子包下所有 @Component 注解
```

### `@EnableAutoConfiguration` 做了什么？
Spring Boot 会根据你加的依赖，**自动配置**相关组件。
- 你加了 `spring-boot-starter-web` → Spring 自动配置内嵌 Tomcat、Spring MVC
- 你加了 `spring-boot-starter-data-jpa` → Spring 自动配置 JPA、数据源
- 你不需要写任何 XML 配置！这就是 Spring Boot 革命性的地方。

### `@ComponentScan` 做了什么？
Spring 会扫描 **启动类所在包及其所有子包** 中带有 `@Component`、`@Service` 等注解的类，并把它们注册为 Bean。

**重要规则：** 这就是为什么所有代码都必须放在 `com.example.bookshelf` 包下或其子包下！

### 代码示例
```java
// BookshelfApplication.java（不需要修改，理解它就行）
package com.example.bookshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookshelfApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookshelfApplication.class, args);
    }
}
```

### 面试题
> **Q：`@SpringBootApplication` 由哪三个注解组成？**
> A：`@SpringBootConfiguration`（或 `@Configuration`）+ `@EnableAutoConfiguration` + `@ComponentScan`

---

## 注解 2：`@Component` 及其四个子注解

### 是什么？
`@Component` 告诉 Spring："请把这个类注册为 Bean，由你来管理"。

Spring 提供了四个语义化的子注解，功能完全相同，但表达的**层次不同**：

| 注解 | 用在哪里 | 含义 |
|------|---------|------|
| `@Component` | 通用组件 | 不属于以下任何层 |
| `@Controller` | 控制层 | 接收 HTTP 请求 |
| `@Service` | 业务层 | 处理业务逻辑 |
| `@Repository` | 数据层 | 操作数据库 |

### 为什么要区分这四个？
1. **代码可读性**：一看注解就知道这个类是什么层的
2. **功能差异**：`@Repository` 会将数据库异常转换为 Spring 的统一异常
3. **大厂规范**：严格按三层架构使用对应注解

### 代码示例

现在创建一个简单的测试组件，理解 Spring 是怎么管理 Bean 的：

**创建文件：`src/main/java/com/example/bookshelf/component/AppInfo.java`**
```java
package com.example.bookshelf.component;

import org.springframework.stereotype.Component;

// 告诉 Spring：请把这个类创建成 Bean 并管理起来
@Component
public class AppInfo {

    private String name = "BookShelf 书架系统";
    private String version = "1.0.0";

    public String getInfo() {
        return name + " v" + version;
    }
}
```

---

## 注解 3：`@Bean`

### 是什么？
`@Bean` 用在**方法**上（而不是类上），表示这个方法的返回值是一个 Bean，交给 Spring 管理。

### 什么时候用 `@Bean`？
- 当你要使用**第三方库的类**作为 Bean 时
- 第三方类你改不了源码，加不了 `@Component`
- 你需要对 Bean 的创建过程有精细控制时

### 和 `@Component` 的区别

| | `@Component` | `@Bean` |
|--|-------------|---------|
| 用在 | 类上 | 方法上 |
| 适用 | 自己写的类 | 第三方类 |
| 搭配 | 无需额外注解 | 必须在 `@Configuration` 类里 |

### 代码示例

**创建文件：`src/main/java/com/example/bookshelf/config/AppConfig.java`**
```java
package com.example.bookshelf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

// @Configuration 表示这是一个配置类，里面可以定义 @Bean
@Configuration
public class AppConfig {

    // 假设我们需要一个 Clock 对象（第三方类，我们没法加 @Component）
    // 用 @Bean 方法来创建它
    @Bean
    public Clock systemClock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }

    // 方法名 "systemClock" 就是这个 Bean 的名字（默认）
}
```

---

## 注解 4：`@Autowired`

### 是什么？
`@Autowired` 告诉 Spring："请把我需要的 Bean 注入进来"。
Spring 会从容器中找对应类型的 Bean，自动赋值给这个字段。

### 三种注入方式

**方式一：字段注入（不推荐！）**
```java
@Component
public class SomeClass {
    @Autowired  // Spring 会找到 AppInfo 这个 Bean 注入进来
    private AppInfo appInfo;
}
// 缺点：无法做单元测试，字段是 private 的，不能在测试中替换
```

**方式二：Setter 注入（较少用）**
```java
@Component
public class SomeClass {
    private AppInfo appInfo;

    @Autowired
    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }
}
```

**方式三：构造器注入（推荐！大厂标准写法）**
```java
@Component
public class SomeClass {
    private final AppInfo appInfo;  // final 保证不可变

    // 当类只有一个构造器时，@Autowired 可以省略（Spring 4.3+）
    @Autowired  // 或者直接省略这行
    public SomeClass(AppInfo appInfo) {
        this.appInfo = appInfo;
    }
}
```

### 为什么构造器注入是最佳实践？
1. `final` 字段，对象不可变，线程安全
2. 单元测试时可以直接 `new`，不依赖 Spring
3. 如果依赖缺失，启动时就报错（而不是运行时才空指针）
4. 当构造器参数太多，说明类职责太多，提醒你重构

> **大厂 Tip：** 配合 Lombok 的 `@RequiredArgsConstructor`，一行注解自动生成构造器。Lab 9 会讲。

---

## 注解 5：`@Value`

### 是什么？
`@Value` 把 `application.yml` 中的配置值注入到字段里。

### 代码示例

**修改 `application.yml`，添加自定义配置：**
```yaml
spring:
  application:
    name: bookshelf

server:
  port: 8080

# 自定义配置
app:
  name: 书架管理系统
  version: 1.0.0
  max-books-per-page: 20
```

**在 Java 类中使用 `@Value`：**
```java
package com.example.bookshelf.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppInfo {

    // ${} 语法：读取 yml 中对应 key 的值
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String version;

    // 冒号后面是默认值（当 yml 中没有这个 key 时使用）
    @Value("${app.max-books-per-page:10}")
    private int maxBooksPerPage;

    // 可以注入 Spring 内置变量
    @Value("${spring.application.name}")
    private String springAppName;

    public String getInfo() {
        return appName + " v" + version + "，每页最多 " + maxBooksPerPage + " 本书";
    }
}
```

### `@Value` vs `@ConfigurationProperties`
`@Value` 适合注入**单个值**。如果有一组相关配置，用 `@ConfigurationProperties`（Lab 8 讲）更好。

---

## 动手实践：验证 IoC 注入

**创建文件：`src/main/java/com/example/bookshelf/component/StartupRunner.java`**

```java
package com.example.bookshelf.component;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// CommandLineRunner：Spring Boot 启动完成后自动执行 run 方法
// 常用于启动时初始化数据、打印配置信息
@Component
public class StartupRunner implements CommandLineRunner {

    private final AppInfo appInfo;

    // 构造器注入（推荐写法）
    // Spring 自动把 AppInfo 这个 Bean 注入进来
    public StartupRunner(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    @Override
    public void run(String... args) {
        System.out.println("========================================");
        System.out.println("应用启动成功！");
        System.out.println(appInfo.getInfo());
        System.out.println("========================================");
    }
}
```

**运行程序，控制台会打印：**
```
========================================
应用启动成功！
书架管理系统 v1.0.0，每页最多 20 本书
========================================
```

---

## 面试考点

> **Q：@Component、@Service、@Repository、@Controller 有什么区别？**
> A：功能上都是把类注册为 Spring Bean，区别在语义层次。`@Repository` 额外提供数据库异常转换功能。大厂规范要求严格按层使用对应注解。

> **Q：@Bean 和 @Component 的区别？**
> A：`@Component` 加在自己写的类上，Spring 自动扫描。`@Bean` 加在配置类的方法上，用于注册第三方类或需要精细控制创建过程的 Bean。

> **Q：三种注入方式哪种最好，为什么？**
> A：构造器注入最好。原因：字段 final 不可变保证线程安全、便于单元测试（不依赖 Spring）、依赖缺失在启动时就报错。

> **Q：@Autowired 注入时，如果有多个同类型的 Bean 怎么办？**
> A：用 `@Qualifier("beanName")` 指定 Bean 名称，或用 `@Primary` 标记优先使用的 Bean。

---

## 本章小结

你已经学会了：
- ✅ IoC 容器的核心思想
- ✅ `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`
- ✅ 四个组件注解：`@Component`、`@Service`、`@Repository`、`@Controller`
- ✅ `@Bean`：用于注册第三方类
- ✅ `@Autowired`：三种注入方式，构造器注入是最佳实践
- ✅ `@Value`：从 yml 读取配置值

---

下一步 → [Lab 2: 第一个 REST API](./Lab2-REST-API基础.md)
