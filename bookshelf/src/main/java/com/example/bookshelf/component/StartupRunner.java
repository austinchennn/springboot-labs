package com.example.bookshelf.component;

// TODO(Lab1): 加上 @Component 注解
// TODO(Lab1): 让这个类实现 CommandLineRunner 接口（Spring Boot 启动完成后自动执行 run 方法）
public class StartupRunner {

    private final AppInfo appInfo;

    // 构造器注入（推荐写法，Lab1 讲过为什么）
    public StartupRunner(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    // TODO(Lab1): 实现 run 方法（来自 CommandLineRunner 接口），
    // 启动时打印 appInfo.getInfo() 的内容，验证 IoC 注入是否成功
    public void run(String... args) {
        throw new UnsupportedOperationException("TODO: 实现 run()");
    }
}
