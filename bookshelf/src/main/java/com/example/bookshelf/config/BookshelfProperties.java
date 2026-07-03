package com.example.bookshelf.config;

// 批量绑定 application.yml 中 app.bookshelf.* 前缀下的配置（Lab8 "@ConfigurationProperties"）
// TODO(Lab8): 加上 @Component 和 @ConfigurationProperties(prefix = "app.bookshelf")
public class BookshelfProperties {

    // 字段名与 yml 中的 key 自动对应（驼峰 ↔ 连字符），例如 maxBooksPerPage ↔ max-books-per-page
    private int maxBooksPerPage = 10;
    private String defaultCategory = "综合";
    private int lowStockThreshold = 5;
    private boolean cacheEnabled = false;
    private boolean auditLogEnabled = false;

    // Spring 通过 Setter 注入配置值，Getter/Setter 必须都有
    public int getMaxBooksPerPage() { return maxBooksPerPage; }
    public void setMaxBooksPerPage(int maxBooksPerPage) { this.maxBooksPerPage = maxBooksPerPage; }
    public String getDefaultCategory() { return defaultCategory; }
    public void setDefaultCategory(String defaultCategory) { this.defaultCategory = defaultCategory; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }
    public boolean isAuditLogEnabled() { return auditLogEnabled; }
    public void setAuditLogEnabled(boolean auditLogEnabled) { this.auditLogEnabled = auditLogEnabled; }
}
