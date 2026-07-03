package com.example.bookshelf.config;

import java.time.Clock;
import java.time.ZoneId;

// TODO(Lab1): 加上 @Configuration 注解，表示这是一个配置类
public class AppConfig {

    // TODO(Lab1): 加上 @Bean 注解，把这个方法的返回值注册成 Bean
    // 提示：第三方类（如 Clock）没法加 @Component，只能用 @Bean 的方式注册
    public Clock systemClock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }
}
