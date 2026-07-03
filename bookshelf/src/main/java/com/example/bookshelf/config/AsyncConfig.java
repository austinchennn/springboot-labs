package com.example.bookshelf.config;

// 生产环境用的线程池配置（Lab11 "配置线程池"）
// 默认 @Async 用 SimpleAsyncTaskExecutor（每次新建线程，不能用于生产）
// TODO(Lab11): 加上 @Configuration 和 @EnableAsync 注解
public class AsyncConfig {

    // TODO(Lab11): 加上 @Bean("taskExecutor")，返回一个配置好的 ThreadPoolTaskExecutor：
    //   corePoolSize=5, maxPoolSize=20, queueCapacity=100,
    //   threadNamePrefix="Async-", rejectedExecutionHandler=CallerRunsPolicy
    //   别忘了最后调用 executor.initialize()
    public Object taskExecutor() {
        throw new UnsupportedOperationException("TODO: 实现 taskExecutor()，返回 Executor 类型");
    }
}
