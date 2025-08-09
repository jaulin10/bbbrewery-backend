package com.bbbrewery.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

@Entity
@Table(name = "BB_BASKETITEM")
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basketitem_seq")
    @SequenceGenerator(name = "basketitem_seq", sequenceName = "BB_BASKETITEM_SEQ", allocationSize = 1)
    @Column(name = "IDBASKETITEM")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDBASKET", referencedColumnName = "IDBASKET")
    private Basket basket;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCT", referencedColumnName = "IDPRODUCT")
    private Product product;

    @NotNull
    @Min(value = 1, message = "La quantité doit être au moins 1")
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "PRICE", precision = 8, scale = 2, nullable = false)
    private BigDecimal price;

    // ========== ATTENTION: Colonnes OPTION comme NUMBER (pas VARCHAR) ==========
    @Column(name = "OPTION1")
    private Integer option1;  // ← NUMBER en base, donc Integer en Java

    @Column(name = "OPTION2")
    private Integer option2;  // ← NUMBER en base, donc Integer en Java

    // Constructeurs
    public BasketItem() {}

    public BasketItem(Basket basket, Product product, Integer quantity, BigDecimal price) {
        this.basket = basket;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    // Méthodes utilitaires
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductName() {
        return product != null ? product.getProductName() : null;
    }

    public boolean hasOptions() {
        return option1 != null || option2 != null;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Basket getBasket() {
        return basket;
    }

    public void setBasket(Basket basket) {
        this.basket = basket;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getOption1() {
        return option1;
    }

    public void setOption1(Integer option1) {
        this.option1 = option1;
    }

    public Integer getOption2() {
        return option2;
    }

    public void setOption2(Integer option2) {
        this.option2 = option2;
    }

    @Override
    public String toString() {
        return "BasketItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + getSubtotal() +
                ", option1=" + option1 +
                ", option2=" + option2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasketItem)) return false;
        BasketItem that = (BasketItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}