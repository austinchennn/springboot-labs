# Lab 0: 环境搭建与项目创建

## 学习目标
- 安装好所有开发工具
- 用 Spring Initializr 创建项目
- 理解 Spring Boot 项目结构
- 成功运行第一个程序

---

## 第一步：安装 JDK 17

### 为什么是 JDK 17？
Spring Boot 3.x 要求 **Java 17 或更高版本**。Java 17 是 LTS（长期支持版），大厂基本都在用。

### 安装步骤

**macOS（推荐用 Homebrew）：**
```bash
brew install openjdk@17
# 安装完后配置环境变量（按提示执行）
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Windows：**
1. 去 https://adoptium.net 下载 JDK 17
2. 安装时勾选 "Set JAVA_HOME"
3. 安装完毕

**验证安装成功：**
```bash
java -version
# 应该显示：openjdk version "17.x.x" ...
```

---

## 第二步：安装 Maven

Maven 是 Java 的包管理工具（类似 Python 的 pip，Node 的 npm）。用来下载依赖、编译项目、打包部署。

**macOS：**
```bash
brew install maven
```

**Windows：**
1. 去 https://maven.apache.org/download.cgi 下载
2. 解压到 `C:\Program Files\Maven`
3. 添加 `C:\Program Files\Maven\bin` 到系统 PATH

**验证：**
```bash
mvn -version
# 应该显示：Apache Maven 3.x.x
```

---

## 第三步：安装 IntelliJ IDEA

推荐使用 **IntelliJ IDEA Community Edition**（免费），Spring Boot 支持最好。

下载地址：https://www.jetbrains.com/idea/download/

安装完成后，安装以下插件（File → Settings → Plugins）：
- **Lombok**（必装！否则 Lombok 注解会报红）
- **Spring Boot Assistant**（可选，提供 yml 自动补全）

---

## 第四步：创建 Spring Boot 项目

### 使用 Spring Initializr

打开浏览器访问：**https://start.spring.io**

按以下配置填写：

| 字段 | 值 |
|------|-----|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.x（选最新稳定版） |
| Group | com.example |
| Artifact | bookshelf |
| Name | bookshelf |
| Package name | com.example.bookshelf |
| Packaging | Jar |
| Java | 17 |

**添加以下依赖（搜索并勾选）：**
- Spring Web
- Spring Data JPA
- H2 Database
- Validation
- Lombok
- Spring AOP（搜索 "aop"）

点击 **GENERATE** 下载 zip 文件，解压后用 IntelliJ IDEA 打开。

---

## 第五步：理解 pom.xml

打开项目根目录的 `pom.xml`，这是 Maven 的配置文件，相当于项目的"说明书"。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Spring Boot 父项目，管理所有依赖的版本，让我们不用操心版本冲突 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <!-- 项目基本信息 -->
    <groupId>com.example</groupId>
    <artifactId>bookshelf</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>bookshelf</name>
    <description>书架管理系统 - Spring Boot 学习项目</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Web：提供 REST API 能力，包含 Spring MVC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA：操作数据库的框架 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Boot Validation：数据校验 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring AOP：切面编程 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- Spring Cache：缓存支持 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <!-- H2：内存数据库，无需安装 MySQL，启动就能用 -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok：减少重复代码的工具 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 第六步：理解项目结构

```
bookshelf/
├── src/
│   ├── main/
│   │   ├── java/com/example/bookshelf/
│   │   │   └── BookshelfApplication.java   ← 程序入口，只有这一个文件
│   │   └── resources/
│   │       ├── application.properties       ← 配置文件（我们会改成 .yml）
│   │       ├── static/                      ← 静态资源（图片、JS、CSS）
│   │       └── templates/                   ← 模板文件（我们不用，我们做纯 API）
│   └── test/
│       └── java/com/example/bookshelf/
│           └── BookshelfApplicationTests.java
├── pom.xml                                  ← Maven 配置
└── README.md
```

**关键理解：**
- `src/main/java`：你写的所有 Java 代码放这里
- `src/main/resources`：配置文件放这里
- `src/test/java`：测试代码放这里
- `pom.xml`：项目依赖声明

---

## 第七步：修改配置文件

将 `src/main/resources/application.properties` **重命名**为 `application.yml`

然后输入以下内容：

```yaml
# application.yml
spring:
  application:
    name: bookshelf   # 应用名称

  # H2 内存数据库配置
  datasource:
    url: jdbc:h2:mem:bookshelfdb   # 内存数据库，程序停止数据清空
    driver-class-name: org.h2.Driver
    username: sa
    password:

  # JPA 配置
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop   # 启动时建表，停止时删表（开发环境用）
    show-sql: true             # 控制台打印 SQL 语句，方便调试

  # H2 控制台（浏览器可视化数据库，开发用）
  h2:
    console:
      enabled: true
      path: /h2-console

# 服务器配置
server:
  port: 8080   # 启动端口
```

---

## 第八步：查看入口类

打开 `BookshelfApplication.java`：

```java
package com.example.bookshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication 是 Spring Boot 最核心的注解！
// 我们在 Lab 1 会详细讲它。现在先知道：有了它，程序才能启动。
@SpringBootApplication
public class BookshelfApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(BookshelfApplication.class, args);
    }
}
```

---

## 第九步：运行项目

**在 IntelliJ IDEA 中：**
1. 打开 `BookshelfApplication.java`
2. 点击左侧绿色三角形 ▶ 运行
3. 或者右键 → Run 'BookshelfApplication'

**用 Maven 命令运行（在项目根目录）：**
```bash
mvn spring-boot:run
```

**看到以下输出说明成功：**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started BookshelfApplication in 2.345 seconds
```

**验证运行成功：**
打开浏览器访问 http://localhost:8080/h2-console
- JDBC URL 填：`jdbc:h2:mem:bookshelfdb`
- 点 Connect，能进去就说明一切正常！

---

## 本章小结

你已经完成了：
- ✅ 安装 JDK 17、Maven、IntelliJ IDEA
- ✅ 创建 Spring Boot 项目
- ✅ 理解项目结构和 pom.xml
- ✅ 配置 application.yml
- ✅ 成功运行

---

## 下一步

→ [Lab 1: Spring Boot 核心注解](./Lab1-SpringBoot核心.md)
