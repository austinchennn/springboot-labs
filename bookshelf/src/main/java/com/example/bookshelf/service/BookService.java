package com.example.bookshelf.service;

import com.example.bookshelf.dto.BookRequest;
import com.example.bookshelf.dto.BookResponse;

import java.util.List;

// Service 接口：Controller 只依赖这个接口，不依赖具体实现（Lab5 "为什么要有 Service 层"）
public interface BookService {

    List<BookResponse> findAll();

    BookResponse findById(Long id);

    BookResponse create(BookRequest request);

    BookResponse update(Long id, BookRequest request);

    void delete(Long id);
}
