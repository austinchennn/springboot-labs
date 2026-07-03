package com.example.bookshelf.controller;

import com.example.bookshelf.common.ApiResponse;
import com.example.bookshelf.dto.BookRequest;
import com.example.bookshelf.dto.BookResponse;
import com.example.bookshelf.service.BookService;

import java.util.List;

// TODO(Lab2): 加上 @RestController 注解
// TODO(Lab2): 加上 @RequestMapping("/api/books")，作为所有方法的 URL 前缀
public class BookController {

    private final BookService bookService;

    // 构造器注入（推荐写法，Lab1 讲过原因）
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ─────────────────────────────────────────────
    // GET /api/books?keyword=&page=&size=
    // ─────────────────────────────────────────────
    // TODO(Lab2): 加上 @GetMapping
    // TODO(Lab3): 返回类型改成 ResponseEntity<ApiResponse<List<BookResponse>>>
    // TODO(Lab3): 加上 @RequestParam(required = false) String keyword、
    //             @RequestParam(defaultValue = "0") int page、
    //             @RequestParam(defaultValue = "10") int size 三个参数（Lab4 之后可以先用简单版本 findAll()）
    public ApiResponse<List<BookResponse>> getAllBooks() {
        // TODO(Lab4): 调用 bookService.findAll()
        // TODO(Lab7): 用 ApiResponse.success(...) 包装
        // TODO(Lab3): 用 ResponseEntity.ok(...) 包装成 200
        throw new UnsupportedOperationException("TODO: 实现 getAllBooks");
    }

    // ─────────────────────────────────────────────
    // GET /api/books/{id}
    // ─────────────────────────────────────────────
    // TODO(Lab2): 加上 @GetMapping("/{id}")，方法参数加上 @PathVariable Long id
    public ApiResponse<BookResponse> getBookById() {
        // TODO(Lab4): 调用 bookService.findById(id)
        // 找不到时 Service 层会抛 BookNotFoundException，由 GlobalExceptionHandler 处理成 404，
        // 这里不需要自己判断 null（Lab7 之后）
        throw new UnsupportedOperationException("TODO: 实现 getBookById");
    }

    // ─────────────────────────────────────────────
    // GET /api/books/category/{category}
    // ─────────────────────────────────────────────
    // TODO(Lab3): 加上 @GetMapping("/category/{category}")，方法参数加上 @PathVariable String category
    public ApiResponse<List<BookResponse>> getByCategory() {
        // TODO(Lab3): 按分类过滤书籍并返回
        throw new UnsupportedOperationException("TODO: 实现 getByCategory");
    }

    // ─────────────────────────────────────────────
    // POST /api/books
    // ─────────────────────────────────────────────
    // TODO(Lab2): 加上 @PostMapping，方法参数加上 @RequestBody BookRequest request
    // TODO(Lab6): 给参数加上 @Valid，触发校验
    public ApiResponse<BookResponse> addBook() {
        // TODO(Lab4): 调用 bookService.create(request)
        // TODO(Lab3): 用 ResponseEntity.status(HttpStatus.CREATED)... 返回 201（配合 ApiResponse.created(...)）
        throw new UnsupportedOperationException("TODO: 实现 addBook");
    }

    // ─────────────────────────────────────────────
    // PUT /api/books/{id}
    // ─────────────────────────────────────────────
    // TODO(Lab2): 加上 @PutMapping("/{id}")，方法参数加上 @PathVariable Long id、
    //             @Valid @RequestBody BookRequest request
    public ApiResponse<BookResponse> updateBook() {
        // TODO(Lab4): 调用 bookService.update(id, request)
        throw new UnsupportedOperationException("TODO: 实现 updateBook");
    }

    // ─────────────────────────────────────────────
    // DELETE /api/books/{id}
    // ─────────────────────────────────────────────
    // TODO(Lab2): 加上 @DeleteMapping("/{id}")，方法参数加上 @PathVariable Long id
    public void deleteBook() {
        // TODO(Lab4): 调用 bookService.delete(id)
        // TODO(Lab3): 返回 204 No Content（ResponseEntity.noContent().build()）
        throw new UnsupportedOperationException("TODO: 实现 deleteBook");
    }
}
