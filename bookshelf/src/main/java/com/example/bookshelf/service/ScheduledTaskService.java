package com.example.bookshelf.service;

import com.example.bookshelf.repository.BookRepository;

// 定时任务服务（Lab11 "定时任务 @Scheduled"）
// TODO(Lab11): 加上 @Service 注解（可以再加 @RequiredArgsConstructor 代替下面手写的构造器）
public class ScheduledTaskService {

    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    public ScheduledTaskService(BookRepository bookRepository, NotificationService notificationService) {
        this.bookRepository = bookRepository;
        this.notificationService = notificationService;
    }

    // TODO(Lab11): 加上 @Scheduled(fixedRate = 60000)
    // 每 60 秒检查一次低库存书籍：调用 bookRepository.findLowStockBooks(10)，
    // 对每本书调用 notificationService.notifyLowStock(book.getId(), book.getStock())
    public void checkLowStock() {
        throw new UnsupportedOperationException("TODO: 实现 checkLowStock");
    }

    // TODO(Lab11): 加上 @Scheduled(fixedDelay = 30000)（上次执行完成后再等待 30 秒）
    public void cleanupExpiredCache() {
        throw new UnsupportedOperationException("TODO: 实现 cleanupExpiredCache");
    }

    // TODO(Lab11): 加上 @Scheduled(cron = "0 0 2 * * ?")（每天凌晨 2 点执行）
    public void dailyDatabaseBackup() {
        throw new UnsupportedOperationException("TODO: 实现 dailyDatabaseBackup");
    }
}
