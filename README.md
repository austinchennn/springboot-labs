# Spring Boot 从零到面试 — 书架管理系统实战 Lab

## 项目介绍

你将从零开始，用 Spring Boot 3.x 构建一个**书架管理系统（BookShelf API）**。
这是一个真实的后端 REST API，涵盖大厂日常开发的所有常用注解和流程。

**完成全部 Lab 后，你将掌握：**
- Spring Boot 的核心机制与启动原理
- 所有 Spring MVC 的请求/响应注解
- Spring Data JPA 数据持久化
- 服务层、事务、DTO 的正确写法
- 数据校验、全局异常处理
- 配置管理与多环境部署
- Lombok 提效工具
- AOP 切面编程（日志/监控）
- 缓存与异步任务
- 能自信回答面试官关于 Spring 的任何问题

---

## 项目最终结构

```
bookshelf/
├── src/main/java/com/example/bookshelf/
│   ├── BookshelfApplication.java       ← 入口
│   ├── controller/
│   │   └── BookController.java         ← 接收请求
│   ├── service/
│   │   ├── BookService.java            ← 接口
│   │   └── impl/BookServiceImpl.java   ← 实现
│   ├── repository/
│   │   └── BookRepository.java         ← 数据库操作
│   ├── entity/
│   │   └── Book.java                   ← 数据库表映射
│   ├── dto/
│   │   ├── BookRequest.java            ← 接收前端数据
│   │   └── BookResponse.java           ← 返回给前端数据
│   ├── exception/
│   │   ├── BookNotFoundException.java  ← 自定义异常
│   │   └── GlobalExceptionHandler.java ← 全局处理
│   ├── config/
│   │   └── AppConfig.java              ← 配置类
│   └── aspect/
│       └── LoggingAspect.java          ← 日志切面
└── src/main/resources/
    ├── application.yml                 ← 主配置
    ├── application-dev.yml             ← 开发环境
    └── application-prod.yml            ← 生产环境
```

---

## Lab 目录

| Lab | 主题 | 关键注解 |
|-----|------|---------|
| **Lab 0** | 环境搭建与项目创建 | — |
| **Lab 1** | Spring Boot 核心原理 | `@SpringBootApplication` `@Component` `@Bean` `@Autowired` `@Value` |
| **Lab 2** | 第一个 REST API | `@RestController` `@RequestMapping` `@GetMapping` `@PostMapping` `@PutMapping` `@DeleteMapping` |
| **Lab 3** | 请求参数详解 | `@PathVariable` `@RequestParam` `@RequestBody` `ResponseEntity` |
| **Lab 4** | 数据持久化 JPA | `@Entity` `@Table` `@Id` `@GeneratedValue` `@Column` `@Repository` `@Query` |
| **Lab 5** | 服务层与事务 | `@Service` `@Transactional` DTO 模式 |
| **Lab 6** | 数据校验 | `@Valid` `@Validated` `@NotNull` `@Size` `@Email` 等 |
| **Lab 7** | 全局异常处理 | `@RestControllerAdvice` `@ExceptionHandler` `@ResponseStatus` |
| **Lab 8** | 配置管理 | `@Configuration` `@ConfigurationProperties` `@Profile` |
| **Lab 9** | Lombok 提效 | `@Data` `@Builder` `@Slf4j` `@RequiredArgsConstructor` |
| **Lab 10** | AOP 切面编程 | `@Aspect` `@Before` `@After` `@Around` |
| **Lab 11** | 缓存与异步 | `@Cacheable` `@CacheEvict` `@Async` `@Scheduled` |
| **Lab 12** | 面试总结与注解全图 | 全部注解快速回顾 + 高频面试题 |

---

## 技术栈

- Java 17
- Spring Boot 3.2.x
- Spring Web (MVC)
- Spring Data JPA
- H2 内存数据库（无需安装 MySQL，直接运行）
- Lombok
- Spring Validation
- Spring AOP

---

## 如何使用这套 Lab

1. 按顺序从 Lab 0 开始
2. **先读概念介绍**，再动手写代码
3. 每一步都有完整代码，直接复制后理解它
4. 每个 Lab 末尾有**面试考点**，完成后记得记忆
5. 遇到不懂的注解，查看对应 Lab 的详细说明

> **注意**：每个 Lab 的代码是累积的，前一个 Lab 的代码是下一个的基础。
> 不要跳过任何一个 Lab。

---

开始你的第一个 Lab → [Lab 0: 环境搭建](./Lab0-环境搭建.md)
