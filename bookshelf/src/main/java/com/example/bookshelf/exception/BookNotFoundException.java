package com.example.bookshelf.exception;

// 自定义异常：书籍不存在。继承 RuntimeException（不是 Checked Exception），
// 这样 @Transactional 才会自动回滚（见 Lab7 "为什么继承 RuntimeException"）
public class BookNotFoundException extends RuntimeException {

    private final Long bookId;

    // TODO(Lab7): 调用 super("书籍不存在，ID: " + bookId)，并把 bookId 赋值给字段
    public BookNotFoundException(Long bookId) {
        this.bookId = bookId;
    }

    public Long getBookId() {
        return bookId;
    }
}
