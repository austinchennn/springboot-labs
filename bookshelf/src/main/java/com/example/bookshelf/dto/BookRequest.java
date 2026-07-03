package com.example.bookshelf.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// 前端创建/更新书籍时发送的数据，不包含 id、createdAt、updatedAt（由后端管理）
// TODO(Lab9): 加上 @Data（Lombok），然后删掉下面手写的 getter/setter
public class BookRequest {

    // TODO(Lab6): 加上 @NotBlank(message = "书名不能为空")、@Size(max = 200, ...)
    private String title;

    // TODO(Lab6): 加上 @NotBlank(message = "作者不能为空")、@Size(max = 100, ...)
    private String author;

    // TODO(Lab6): 加上 @NotBlank + @Pattern(regexp = "^\\d{3}-\\d{10}$", ...) 校验 ISBN 格式
    private String isbn;

    // TODO(Lab6): 加上 @Size(max = 50, ...)
    private String category;

    // TODO(Lab6): 加上 @NotNull + @DecimalMin("0.01") + @DecimalMax("99999.99")
    private BigDecimal price;

    // TODO(Lab6): 加上 @NotNull + @Min(0) + @Max(99999)
    private Integer stock;

    // TODO(Lab6): 加上 @Past(message = "出版日期必须是过去的日期")
    private LocalDate publishDate;

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
}
