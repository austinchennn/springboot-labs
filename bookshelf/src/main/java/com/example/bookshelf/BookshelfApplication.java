package com.example.bookshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication 是 Spring Boot 最核心的注解，Lab 1 会详细讲它
@SpringBootApplication
// TODO(Lab11): 加上 @EnableCaching、@EnableAsync、@EnableScheduling 三个注解，
// 分别开启 Spring Cache、异步执行、定时任务功能
public class BookshelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookshelfApplication.class, args);
    }
}
