package com.example.bookshelf.component;

// TODO(Lab1): 加上 @Component 注解，把这个类注册成 Spring Bean
public class AppInfo {

    // TODO(Lab1): 用 @Value("${app.name}") 从 application.yml 读取应用名称
    private String appName;

    // TODO(Lab1): 用 @Value("${app.version}") 读取版本号
    private String version;

    // TODO(Lab1): 用 @Value("${app.max-books-per-page:10}") 读取每页最大数量，冒号后是默认值
    private int maxBooksPerPage;

    // TODO(Lab1): 实现 getInfo()，拼接返回 "书架管理系统 v1.0.0，每页最多 20 本书" 这样的字符串
    public String getInfo() {
        throw new UnsupportedOperationException("TODO: 实现 getInfo()");
    }
}
