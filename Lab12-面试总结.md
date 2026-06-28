# Lab 12: 面试总结与注解全图

## 你完成了什么？

做完这套 Lab，你从零构建了一个完整的企业级 REST API，覆盖了：
- ✅ 三层架构（Controller → Service → Repository）
- ✅ 完整的 CRUD 操作（Book 管理）
- ✅ 数据校验 + 统一异常处理 + 统一响应格式
- ✅ 多环境配置（dev/prod）
- ✅ Lombok 减少样板代码
- ✅ AOP 日志切面 + 审计日志
- ✅ 缓存 + 异步 + 定时任务

---

## 所有注解速查表

### 核心启动注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@SpringBootApplication` | 启动类 | = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| `@EnableCaching` | 启动类 | 开启 Spring Cache |
| `@EnableAsync` | 启动类 | 开启异步支持 |
| `@EnableScheduling` | 启动类 | 开启定时任务 |

### 组件注册注解（Bean 相关）

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Component` | 类 | 通用组件，注册为 Spring Bean |
| `@Controller` | 类 | 控制层，返回视图（传统 MVC） |
| `@RestController` | 类 | REST API 控制器，= `@Controller` + `@ResponseBody` |
| `@Service` | 类 | 业务层 |
| `@Repository` | 类/接口 | 数据层，额外提供异常转换 |
| `@Configuration` | 类 | 配置类，可定义 `@Bean` |
| `@Bean` | 方法 | 注册第三方类为 Bean，在 `@Configuration` 类里使用 |
| `@Autowired` | 字段/构造器/Setter | 依赖注入（推荐构造器注入） |
| `@Qualifier("name")` | 字段/参数 | 当有多个同类型 Bean 时，指定名称 |
| `@Primary` | 类/方法 | 同类型多 Bean 时标记优先使用的 |

### Spring MVC 注解（控制层）

| 注解 | 位置 | 作用 |
|------|------|------|
| `@RequestMapping` | 类/方法 | URL 路径映射（类级别定义前缀） |
| `@GetMapping` | 方法 | 处理 GET 请求 |
| `@PostMapping` | 方法 | 处理 POST 请求 |
| `@PutMapping` | 方法 | 处理 PUT 请求（全量更新） |
| `@PatchMapping` | 方法 | 处理 PATCH 请求（部分更新） |
| `@DeleteMapping` | 方法 | 处理 DELETE 请求 |
| `@PathVariable` | 参数 | 获取 URL 路径变量（`/books/{id}`） |
| `@RequestParam` | 参数 | 获取查询参数（`?keyword=java`） |
| `@RequestBody` | 参数 | 获取请求体 JSON，反序列化为对象 |
| `@ResponseBody` | 方法/类 | 返回值序列化为 JSON（`@RestController` 已包含） |

### JPA / 数据库注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Entity` | 类 | 声明 JPA 实体（映射数据库表） |
| `@Table(name="")` | 类 | 指定表名 |
| `@Id` | 字段 | 声明主键 |
| `@GeneratedValue(strategy=)` | 字段 | 主键生成策略（IDENTITY/SEQUENCE/AUTO） |
| `@Column` | 字段 | 列属性（nullable、unique、length 等） |
| `@ManyToOne` | 字段 | 多对一关系 |
| `@OneToMany` | 字段 | 一对多关系（通常加 `mappedBy`） |
| `@JoinColumn` | 字段 | 外键列配置 |
| `@Query` | Repository 方法 | 自定义 JPQL 或原生 SQL |

### 事务注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Transactional` | 类/方法 | 声明事务（`readOnly`、`rollbackFor`、`propagation`、`isolation`） |

### 校验注解（加在 DTO 字段上）

| 注解 | 作用 |
|------|------|
| `@NotNull` | 不允许 null |
| `@NotBlank` | 不允许 null、空串、纯空格（String 最常用）|
| `@NotEmpty` | 不允许 null 和空 |
| `@Size(min,max)` | 字符串长度或集合大小 |
| `@Min(value)` / `@Max(value)` | 数值最小/最大值 |
| `@Positive` / `@PositiveOrZero` | 正数 / 非负数 |
| `@Email` | 邮箱格式 |
| `@Pattern(regexp)` | 正则匹配 |
| `@Past` / `@Future` | 过去 / 未来的日期 |
| `@Valid` | 触发校验（加在 Controller 方法参数前） |
| `@Validated` | 触发校验（支持分组，可用于 Service 层） |

### 异常处理注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@RestControllerAdvice` | 类 | 全局异常处理器（返回 JSON） |
| `@ControllerAdvice` | 类 | 全局异常处理器（可返回视图） |
| `@ExceptionHandler(Ex.class)` | 方法 | 处理特定类型的异常 |
| `@ResponseStatus(HttpStatus.xxx)` | 类/方法 | 指定 HTTP 状态码 |

### 配置相关注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Value("${key}")` | 字段 | 注入单个配置值 |
| `@ConfigurationProperties(prefix=)` | 类 | 批量绑定 yml 配置 |
| `@Profile("env")` | 类/方法 | 指定激活环境 |
| `@ConditionalOnProperty` | 类/方法 | 按配置属性条件装配 |

### Lombok 注解

| 注解 | 作用 |
|------|------|
| `@Getter` / `@Setter` | 生成 Getter/Setter |
| `@Data` | = `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor` |
| `@Builder` | Builder 模式 |
| `@NoArgsConstructor` | 无参构造器 |
| `@AllArgsConstructor` | 全参构造器 |
| `@RequiredArgsConstructor` | final 字段构造器（依赖注入最常用） |
| `@Slf4j` | 注入 `log` 日志字段 |
| `@ToString(exclude=)` | 生成 toString（可排除字段） |
| `@EqualsAndHashCode` | 生成 equals/hashCode |

### AOP 注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Aspect` | 类 | 声明切面类（必须配合 `@Component`） |
| `@Pointcut` | 方法 | 定义可复用的切点表达式 |
| `@Before` | 方法 | 前置通知（方法执行前） |
| `@After` | 方法 | 最终通知（方法执行后，类似 finally） |
| `@AfterReturning` | 方法 | 返回通知（正常返回后，可获取返回值） |
| `@AfterThrowing` | 方法 | 异常通知（抛出异常后，可获取异常） |
| `@Around` | 方法 | 环绕通知（最强大，控制是否执行） |
| `@Order(n)` | 类 | 多切面时控制执行顺序（n 越小越先执行） |

### 缓存 / 异步 / 定时注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Cacheable` | 方法 | 缓存方法结果 |
| `@CachePut` | 方法 | 每次执行并更新缓存 |
| `@CacheEvict` | 方法 | 清除缓存 |
| `@Async` | 方法 | 异步执行（另一个线程） |
| `@Scheduled` | 方法 | 定时执行（fixedRate / fixedDelay / cron） |

---

## 注解层级关系图

```
Spring IoC 容器
├── Bean 注册
│   ├── @Component（通用）
│   │   ├── @Controller（控制层）
│   │   │   └── @RestController（REST API，含 @ResponseBody）
│   │   ├── @Service（业务层）
│   │   ├── @Repository（数据层）
│   │   └── @Configuration（配置类）
│   └── @Bean（方法级，注册第三方类）
│
├── 依赖注入
│   ├── @Autowired（按类型注入）
│   ├── @Qualifier（按名称指定）
│   └── @Value（注入配置值）
│
└── Spring MVC
    ├── 路由映射
    │   ├── @RequestMapping（类/方法）
    │   ├── @GetMapping / @PostMapping / @PutMapping / @DeleteMapping
    │   └── ...
    └── 参数绑定
        ├── @PathVariable（路径变量 /books/{id}）
        ├── @RequestParam（查询参数 ?key=value）
        └── @RequestBody（请求体 JSON）

JPA 数据层
├── 实体映射
│   ├── @Entity + @Table → 表
│   ├── @Id + @GeneratedValue → 主键
│   ├── @Column → 列属性
│   └── @ManyToOne / @OneToMany → 关联关系
└── 查询
    └── @Query（自定义 JPQL/SQL）

横切关注点
├── @Transactional（事务）
├── @Valid / @Validated（校验）
├── @RestControllerAdvice + @ExceptionHandler（异常处理）
├── @Aspect + @Before/After/Around（AOP 切面）
└── @Cacheable / @Async / @Scheduled（缓存/异步/定时）
```

---

## 高频面试题 100 问（精选 30 问）

### Spring 基础

**Q1：什么是 Spring IoC？**
IoC（控制反转）是一种设计思想，对象的创建和依赖关系由 Spring 容器管理，而不是开发者手动 `new`。这样降低了类之间的耦合度，提高了可测试性。

**Q2：什么是 Spring AOP？**
AOP（面向切面编程）是对 OOP 的补充，把"横切关注点"（日志、事务、权限）从业务逻辑中分离，集中管理。Spring AOP 通过动态代理实现。

**Q3：Spring Bean 的作用域（Scope）有哪些？**
- `singleton`（默认）：整个容器只有一个实例
- `prototype`：每次请求都创建新实例
- `request`：每个 HTTP 请求一个实例（Web）
- `session`：每个 HTTP 会话一个实例（Web）

**Q4：Spring Boot 自动配置原理？**
`@EnableAutoConfiguration` 通过 `spring.factories`（或 Spring Boot 3 的 `AutoConfiguration.imports`）文件找到所有自动配置类，根据 `@ConditionalOn*` 条件决定是否激活。例如引入 web 依赖后，`WebMvcAutoConfiguration` 自动配置 Spring MVC。

### Spring MVC

**Q5：@Controller 和 @RestController 的区别？**
`@RestController = @Controller + @ResponseBody`。`@Controller` 方法返回视图名；`@RestController` 方法返回值自动序列化为 JSON。

**Q6：@PathVariable 和 @RequestParam 的区别？**
`@PathVariable` 从 URL 路径提取（`/books/{id}`），用于标识资源。`@RequestParam` 从查询字符串提取（`?keyword=java`），用于过滤/分页/排序。

**Q7：Spring MVC 的请求处理流程？**
1. 请求到达 `DispatcherServlet`（前端控制器）
2. `HandlerMapping` 找到对应 Controller 方法
3. `HandlerAdapter` 调用方法，解析参数（`@PathVariable`、`@RequestBody` 等）
4. Controller 返回结果
5. `HttpMessageConverter` 将结果序列化（JSON）
6. 返回响应

### JPA

**Q8：JpaRepository 和 CrudRepository 的区别？**
`JpaRepository` 继承 `PagingAndSortingRepository` 继承 `CrudRepository`。`JpaRepository` 提供最完整的功能（批量操作、分页排序、JPA 特有方法）。

**Q9：@OneToMany 的 fetch 默认值？为什么推荐 LAZY？**
默认 `EAGER`（急加载），但推荐全部改为 `LAZY`（懒加载）。EAGER 会在查主表时立即 JOIN 查关联表，很多场景不需要关联数据，造成不必要的性能开销。LAZY 按需加载更高效。

**Q10：什么是 N+1 问题？如何解决？**
查询列表时，主查询返回 N 条记录，每条触发一次关联查询，总共 N+1 次 SQL。解决方案：
1. `@Query` + `JOIN FETCH`
2. `@EntityGraph`
3. DTO 投影（只查需要的字段）

### 事务

**Q11：@Transactional 默认在什么情况下回滚？**
默认只回滚 `RuntimeException` 和 `Error`。`Checked Exception`（如 `IOException`）不回滚！推荐加 `rollbackFor = Exception.class`。

**Q12：@Transactional 的事务传播行为有哪些？最常用的是哪个？**
最常用：
- `REQUIRED`（默认）：有事务就加入，没有就新建
- `REQUIRES_NEW`：无论如何都新建事务，常用于审计日志

**Q13：@Transactional 的自调用失效问题？**
同一个类中方法 A 调方法 B，B 上的 `@Transactional` 不生效，因为自调用不走 Spring 代理。解决：把 B 移到另一个 Bean 中调用。

**Q14：数据库隔离级别有哪几种？**
- `READ_UNCOMMITTED`：最低，可脏读
- `READ_COMMITTED`：防脏读
- `REPEATABLE_READ`：防不可重复读（MySQL 默认）
- `SERIALIZABLE`：最高，防幻读，性能最差

### 校验

**Q15：@NotNull、@NotEmpty、@NotBlank 的区别？**
- `@NotNull`：不允许 null
- `@NotEmpty`：不允许 null 和空串（空格通过）
- `@NotBlank`：不允许 null、空串、纯空格（最严格，字符串最常用）

**Q16：@Valid 和 @Validated 的区别？**
`@Valid` 是 Java EE 标准，`@Validated` 是 Spring 扩展。最大区别：`@Validated` 支持**分组校验**（不同场景校验不同字段）。

### AOP

**Q17：Spring AOP 的实现原理？**
基于动态代理：
- 目标类实现了接口 → JDK 动态代理（`java.lang.reflect.Proxy`）
- 目标类没有接口 → CGLIB 代理（生成子类）
Spring Boot 默认用 CGLIB。

**Q18：@Before、@After、@Around 的执行顺序？**
`@Around 开始` → `@Before` → 目标方法 → `@AfterReturning` 或 `@AfterThrowing` → `@After` → `@Around 结束`

**Q19：AOP 和拦截器的区别？**
AOP 用 Spring 代理实现，可以拦截任何 Spring Bean 的方法（不限于 HTTP 请求）；拦截器（`HandlerInterceptor`）基于 Servlet，只能拦截 HTTP 请求，可以访问 `HttpServletRequest`。

### Spring Boot 高级

**Q20：@ConfigurationProperties 和 @Value 的区别？**
`@Value` 注入单个值，`@ConfigurationProperties` 批量绑定一组配置到 POJO，支持类型安全验证和 IDE 自动补全。一组相关配置优先用 `@ConfigurationProperties`。

**Q21：Spring Boot 的 Starter 机制是什么？**
Starter 是一组依赖的集合（pom.xml），一次引入即可获得相关功能的所有依赖和自动配置。例如 `spring-boot-starter-web` 包含了 Spring MVC、内嵌 Tomcat、Jackson 等。

**Q22：@SpringBootApplication 由哪三个注解组成？**
`@SpringBootConfiguration`（= `@Configuration`）+ `@EnableAutoConfiguration`（自动配置）+ `@ComponentScan`（扫描组件）

**Q23：Spring Boot 多环境配置怎么做？**
创建 `application-dev.yml`、`application-prod.yml`，通过 `spring.profiles.active=dev` 或启动参数 `--spring.profiles.active=prod` 激活。

### 缓存和异步

**Q24：@Cacheable 的 condition 和 unless 的区别？**
`condition` 在方法执行前判断（不能用 `#result`）；`unless` 在方法执行后判断（可以用 `#result`）。`unless = "#result == null"` 表示结果为 null 时不缓存。

**Q25：@Async 的注意事项？**
1. 自调用不生效（和 `@Transactional` 一样）
2. 生产环境必须自定义线程池（默认的 `SimpleAsyncTaskExecutor` 不复用线程）
3. 异常不会传播到调用者（需要通过 `CompletableFuture` 或全局异常处理捕获）

---

## 项目架构全貌（你做完了的）

```
bookshelf/
│
├── 【启动层】
│   └── BookshelfApplication.java (@SpringBootApplication, @EnableCaching, @EnableAsync, @EnableScheduling)
│
├── 【控制层 Controller】
│   └── BookController.java (@RestController, @RequestMapping, @GetMapping/@PostMapping/@PutMapping/@DeleteMapping, @PathVariable, @RequestParam, @RequestBody, @Valid)
│
├── 【服务层 Service】
│   ├── BookService.java（接口）
│   └── BookServiceImpl.java (@Service, @RequiredArgsConstructor, @Slf4j, @Transactional, @Cacheable, @CachePut, @CacheEvict, @AuditLog)
│
├── 【数据层 Repository】
│   └── BookRepository.java (@Repository, extends JpaRepository, @Query)
│
├── 【实体层 Entity】
│   └── Book.java (@Entity, @Table, @Id, @GeneratedValue, @Column, @Getter, @Setter)
│
├── 【DTO 层】
│   ├── BookRequest.java (@Data, @NotBlank, @NotNull, @Size, @Min, @Max, @Pattern, @Past)
│   └── BookResponse.java (@Getter, @Builder, static from())
│
├── 【异常处理】
│   ├── BookNotFoundException.java（RuntimeException）
│   ├── DuplicateIsbnException.java（RuntimeException）
│   └── GlobalExceptionHandler.java (@RestControllerAdvice, @ExceptionHandler)
│
├── 【配置层 Config】
│   ├── AppConfig.java (@Configuration, @Bean)
│   ├── BookshelfProperties.java (@Component, @ConfigurationProperties)
│   └── AsyncConfig.java (@Configuration, @EnableAsync, ThreadPoolTaskExecutor)
│
├── 【切面 Aspect】
│   ├── LoggingAspect.java (@Aspect, @Component, @Before, @AfterReturning, @AfterThrowing, @Around)
│   └── AuditLogAspect.java (@Aspect, @Component, @Around, @annotation)
│
├── 【定时任务】
│   └── ScheduledTaskService.java (@Service, @Scheduled)
│
├── 【自定义注解】
│   └── @AuditLog（自定义注解 + AOP 拦截）
│
└── 【资源配置】
    ├── application.yml（公共配置）
    ├── application-dev.yml（开发）
    └── application-prod.yml（生产）
```

---

## 面试前最后检查清单

完成以下所有项，你就准备好了：

- [ ] 能画出 Spring IoC 容器工作流程
- [ ] 能解释 `@SpringBootApplication` 的三个子注解
- [ ] 能区分 `@Component` 的四个子注解和使用场景
- [ ] 能解释 `@RestController` vs `@Controller`
- [ ] 能区分 `@PathVariable` vs `@RequestParam`
- [ ] 能讲清楚 JPA 的 `@Entity`、`@Id`、`@Column` 基本用法
- [ ] 能解释 `@Transactional` 默认回滚行为和为什么加 `rollbackFor=Exception.class`
- [ ] 能讲出 `REQUIRED` 和 `REQUIRES_NEW` 传播行为的区别
- [ ] 能区分 `@NotNull`、`@NotEmpty`、`@NotBlank`
- [ ] 能解释 AOP 四个核心概念，说出 5 种通知类型
- [ ] 能解释 `@Around` 中 `joinPoint.proceed()` 的作用
- [ ] 能说出 `@Cacheable` 的 `condition` 和 `unless` 的区别
- [ ] 能解释 `@Async` 的线程池问题

---

**恭喜你！完成了整套 Spring Boot 实战 Lab！**

你已经具备了从事 Java 后端开发的核心知识。
下一步建议：
1. 把这个项目放到 GitHub，面试时展示
2. 学习 Spring Security（认证和授权）
3. 学习 MyBatis（很多公司用 MyBatis 而不是 JPA）
4. 学习 Redis、MQ（消息队列）等中间件
