package com.example.bookshelf.dto;

import com.example.bookshelf.entity.Book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// 返回给前端的数据（不直接暴露 Entity，见 Lab5 "DTO 模式"）
// TODO(Lab9): 加上 @Getter @Builder（Lombok），然后 from() 里改用 BookResponse.builder()... .build()
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private LocalDate publishDate;
    private LocalDateTime createdAt;

    // TODO(Lab5): 实现 Entity → DTO 的转换：把 book 的每个字段复制到一个新的 BookResponse 里并返回
    public static BookResponse from(Book book) {
        throw new UnsupportedOperationException("TODO: 实现 BookResponse.from(Book)");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
