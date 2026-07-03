package com.example.bookshelf.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// TODO(Lab4): 加上 @Entity 和 @Table(name = "books")，让这个类映射到数据库表
// TODO(Lab9): 加上 @Getter @Setter（Lombok），然后删掉下面手写的 getter/setter
public class Book {

    // TODO(Lab4): 加上 @Id 和 @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO(Lab4): 加上 @Column(nullable = false, length = 200)
    private String title;

    // TODO(Lab4): 加上 @Column(nullable = false, length = 100)
    private String author;

    // TODO(Lab4): 加上 @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    // TODO(Lab4): 加上 @Column(length = 50)
    private String category;

    // TODO(Lab4): 加上 @Column(precision = 10, scale = 2)
    private BigDecimal price;

    // TODO(Lab4): 加上 @Column(nullable = false)
    private Integer stock;

    // TODO(Lab4): 加上 @Column(name = "publish_date")
    private LocalDate publishDate;

    // TODO(Lab4): 加上 @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // TODO(Lab4): 加上 @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // TODO(Lab9): 手写基于 id 的 equals()/hashCode()（JPA Entity 不要用 @Data 自动生成的版本，
    // 原因见 Lab9-Lombok.md "@Data 在 Entity 上要谨慎" 一节）
}
