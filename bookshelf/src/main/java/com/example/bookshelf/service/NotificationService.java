package com.example.bookshelf.service;

import java.util.concurrent.CompletableFuture;

// 异步通知服务（Lab11 "异步执行 @Async"）
// TODO(Lab11): 加上 @Service 注解
public class NotificationService {

    // TODO(Lab11): 加上 @Async，让这个方法在独立线程中执行
    // 提示：打印开始日志（含 Thread.currentThread().getName()），Thread.sleep(2000) 模拟耗时，再打印完成日志
    public void sendWelcomeEmail(String email) {
        throw new UnsupportedOperationException("TODO: 实现 sendWelcomeEmail");
    }

    // TODO(Lab11): 加上 @Async，返回 CompletableFuture<String>
    public CompletableFuture<String> sendEmailAndGetResult(String email) {
        throw new UnsupportedOperationException("TODO: 实现 sendEmailAndGetResult");
    }

    // TODO(Lab11): 加上 @Async，模拟低库存通知（打印警告日志即可）
    public void notifyLowStock(Long bookId, int stock) {
        throw new UnsupportedOperationException("TODO: 实现 notifyLowStock");
    }
}
