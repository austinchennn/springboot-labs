package com.example.bookshelf.exception;

// 自定义异常：ISBN 重复
public class DuplicateIsbnException extends RuntimeException {

    private final String isbn;

    // TODO(Lab7): 调用 super("ISBN 已存在: " + isbn)，并把 isbn 赋值给字段
    public DuplicateIsbnException(String isbn) {
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }
}
