package com.example.bookshelf.service.impl;

import com.example.bookshelf.config.BookshelfProperties;
import com.example.bookshelf.dto.BookRequest;
import com.example.bookshelf.dto.BookResponse;
import com.example.bookshelf.entity.Book;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.service.BookService;

import java.util.List;

// TODO(Lab4): 加上 @Service 注解
// TODO(Lab9): 加上 @Slf4j（日志）和 @RequiredArgsConstructor（自动生成 final 字段构造器），
// 然后删掉下面手写的构造器
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    // TODO(Lab8): 注入 BookshelfProperties properties，用它的 lowStockThreshold 代替硬编码的阈值
    // private final BookshelfProperties properties;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    // TODO(Lab5): 加上 @Transactional(readOnly = true)（查询操作用只读事务）
    public List<BookResponse> findAll() {
        // TODO(Lab4): 调用 bookRepository.findAll()，用 BookResponse::from 把每个 Book 转成 BookResponse
        throw new UnsupportedOperationException("TODO: 实现 findAll");
    }

    @Override
    // TODO(Lab5): 加上 @Transactional(readOnly = true)
    // TODO(Lab11): 加上 @Cacheable(value = "books", key = "#id", unless = "#result == null")
    public BookResponse findById(Long id) {
        // TODO(Lab4): 调用 bookRepository.findById(id)
        // TODO(Lab7): 找不到时用 .orElseThrow(() -> new BookNotFoundException(id)) 代替返回 null
        throw new UnsupportedOperationException("TODO: 实现 findById");
    }

    @Override
    // TODO(Lab5): 加上 @Transactional(rollbackFor = Exception.class)
    public BookResponse create(BookRequest request) {
        // TODO(Lab7): 如果 bookRepository.existsByIsbn(request.getIsbn()) 为 true，抛出 DuplicateIsbnException
        // TODO(Lab4): 把 request 的字段逐个复制到一个新的 Book 实体上，设置 createdAt/updatedAt = LocalDateTime.now()
        // TODO(Lab4): 调用 bookRepository.save(book)，用 BookResponse.from(...) 转换后返回
        throw new UnsupportedOperationException("TODO: 实现 create");
    }

    @Override
    // TODO(Lab5): 加上 @Transactional(rollbackFor = Exception.class)
    // TODO(Lab11): 加上 @CachePut(value = "books", key = "#id")，更新后同步刷新缓存
    public BookResponse update(Long id, BookRequest request) {
        // TODO(Lab7): 用 bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id)) 查出实体
        // TODO(Lab4): 把 request 的字段更新到实体上，设置 updatedAt = LocalDateTime.now()
        // TODO(Lab4): 调用 bookRepository.save(...)，用 BookResponse.from(...) 转换后返回
        throw new UnsupportedOperationException("TODO: 实现 update");
    }

    @Override
    // TODO(Lab5): 加上 @Transactional(rollbackFor = Exception.class)
    // TODO(Lab11): 加上 @CacheEvict(value = "books", key = "#id")，删除后清除对应缓存
    public void delete(Long id) {
        // TODO(Lab7): 如果 !bookRepository.existsById(id)，抛出 BookNotFoundException(id)
        // TODO(Lab4): 调用 bookRepository.deleteById(id)
        throw new UnsupportedOperationException("TODO: 实现 delete");
    }
}
