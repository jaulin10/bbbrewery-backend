package com.bbbrewery.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BB_PRODUCT")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "BB_PRODID_SEQ", allocationSize = 1)
    @Column(name = "IDPRODUCT")
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 25, message = "Product name cannot exceed 25 characters")
    @Column(name = "PRODUCTNAME", nullable = false, length = 25)
    private String productName;

    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "PRICE", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Column(name = "STOCK", nullable = false)
    private Integer stock;

    @Column(name = "ACTIVE")
    private Boolean active = true;

    @Column(name = "SALEPRICE", precision = 8, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "SALESTARTDATE")
    private LocalDateTime saleStartDate;

    @Column(name = "SALEENDDATE")
    private LocalDateTime saleEndDate;

    @Column(name = "CATEGORY", length = 20)
    private String category;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "IMAGEURL", length = 200)
    private String imageUrl;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    // Constructeurs
    public Product() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public Product(String productName, String description, BigDecimal price, Integer stock) {
        this();
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public Product(String productName, String description, BigDecimal price, Integer stock, String category) {
        this(productName, description, price, stock);
        this.category = category;
    }

    // Méthodes utilitaires
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public boolean isOnSale() {
        if (salePrice == null || saleStartDate == null || saleEndDate == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(saleStartDate) && now.isBefore(saleEndDate);
    }

    public BigDecimal getCurrentPrice() {
        return isOnSale() ? salePrice : price;
    }

    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    public boolean isLowStock(int threshold) {
        return stock != null && stock <= threshold;
    }

    public boolean isActive() {
        return active != null && active;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public LocalDateTime getSaleStartDate() {
        return saleStartDate;
    }

    public void setSaleStartDate(LocalDateTime saleStartDate) {
        this.saleStartDate = saleStartDate;
    }

    public LocalDateTime getSaleEndDate() {
        return saleEndDate;
    }

    public void setSaleEndDate(LocalDateTime saleEndDate) {
        this.saleEndDate = saleEndDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}