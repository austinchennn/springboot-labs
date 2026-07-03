package com.example.bookshelf.repository;

import com.example.bookshelf.entity.Book;

import java.math.BigDecimal;
import java.util.List;

// TODO(Lab4): 让这个接口 extends JpaRepository<Book, Long>，并加上 @Repository 注解
// 加上之后就自动获得 save/findById/findAll/deleteById/existsById/count 等方法，不需要写实现
public interface BookRepository {

    // TODO(Lab4): 按 Spring Data 方法命名规则声明下面这些方法（只写方法签名，不用写实现）：

    // 根据作者查询 → List<Book> findByAuthor(String author);

    // 根据分类查询 → List<Book> findByCategory(String category);

    // 根据书名模糊搜索（Containing = LIKE %keyword%）→ List<Book> findByTitleContaining(String keyword);

    // 根据价格区间查询 → List<Book> findByPriceBetween(BigDecimal min, BigDecimal max);

    // 根据分类查询并按价格升序排序 → List<Book> findByCategoryOrderByPriceAsc(String category);

    // 判断 ISBN 是否存在 → boolean existsByIsbn(String isbn);

    // 根据分类统计数量 → long countByCategory(String category);

    // TODO(Lab4): 用 @Query(value = "...", nativeQuery = true) 写一个原生 SQL，
    // 查询库存低于阈值的书（供 Lab11 的定时任务 checkLowStock 使用）：
    // List<Book> findLowStockBooks(@Param("threshold") int threshold);
}
