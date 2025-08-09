package com.bbbrewery.backend.dto;

import com.bbbrewery.backend.model.BasketStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BasketDTO {

    private Long id;

    @NotNull
    private Long shopperId;

    private String shopperName;

    private LocalDateTime dateCreated;

    private LocalDateTime dateOrdered;

    private Integer quantity;

    private BigDecimal subtotal;

    private BigDecimal shipping;

    private BigDecimal tax;

    private BigDecimal total;

    private BasketStatus orderPlaced;

    private String orderPlacedDescription;

    private List<BasketItemDTO> basketItems;

    private List<TaxDTO> taxes;

    private List<ShippingDTO> shippings;

    // Constructeurs
    public BasketDTO() {}

    public BasketDTO(Long shopperId) {
        this.shopperId = shopperId;
        this.dateCreated = LocalDateTime.now();
        this.orderPlaced = BasketStatus.ACTIVE;
        this.quantity = 0;
        this.subtotal = BigDecimal.ZERO;
        this.shipping = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    // Méthodes utilitaires
    public boolean isEmpty() {
        return basketItems == null || basketItems.isEmpty();
    }

    public boolean isActive() {
        return orderPlaced == BasketStatus.ACTIVE;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShopperId() {
        return shopperId;
    }

    public void setShopperId(Long shopperId) {
        this.shopperId = shopperId;
    }

    public String getShopperName() {
        return shopperName;
    }

    public void setShopperName(String shopperName) {
        this.shopperName = shopperName;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateOrdered() {
        return dateOrdered;
    }

    public void setDateOrdered(LocalDateTime dateOrdered) {
        this.dateOrdered = dateOrdered;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShipping() {
        return shipping;
    }

    public void setShipping(BigDecimal shipping) {
        this.shipping = shipping;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BasketStatus getOrderPlaced() {
        return orderPlaced;
    }

    public void setOrderPlaced(BasketStatus orderPlaced) {
        this.orderPlaced = orderPlaced;
        this.orderPlacedDescription = orderPlaced != null ? orderPlaced.getDescription() : null;
    }

    public String getOrderPlacedDescription() {
        return orderPlacedDescription;
    }

    public void setOrderPlacedDescription(String orderPlacedDescription) {
        this.orderPlacedDescription = orderPlacedDescription;
    }

    public List<BasketItemDTO> getBasketItems() {
        return basketItems;
    }

    public void setBasketItems(List<BasketItemDTO> basketItems) {
        this.basketItems = basketItems;
    }

    public List<TaxDTO> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<TaxDTO> taxes) {
        this.taxes = taxes;
    }

    public List<ShippingDTO> getShippings() {
        return shippings;
    }

    public void setShippings(List<ShippingDTO> shippings) {
        this.shippings = shippings;
    }

    @Override
    public String toString() {
        return "BasketDTO{" +
                "id=" + id +
                ", shopperId=" + shopperId +
                ", orderPlaced=" + orderPlaced +
                ", total=" + total +
                '}';
    }
}

class BasketItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private LocalDateTime dateCreated;
    private String option1;
    private String option2;

    // Constructeurs
    public BasketItemDTO() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }
}

class TaxDTO {
    private Long id;
    private Integer taxType;
    private String taxTypeDescription;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String state;
    private String province;

    // Constructeurs
    public TaxDTO() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTaxType() {
        return taxType;
    }

    public void setTaxType(Integer taxType) {
        this.taxType = taxType;
    }

    public String getTaxTypeDescription() {
        return taxTypeDescription;
    }

    public void setTaxTypeDescription(String taxTypeDescription) {
        this.taxTypeDescription = taxTypeDescription;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}

class ShippingDTO {
    private Long id;
    private BigDecimal shipCost;
    private LocalDateTime shipDateExpected;
    private LocalDateTime shipDateActual;
    private String shippingMethod;
    private String trackingNumber;
    private Integer shippingStatus;
    private String shippingStatusDescription;

    // Constructeurs
    public ShippingDTO() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getShipCost() {
        return shipCost;
    }

    public void setShipCost(BigDecimal shipCost) {
        this.shipCost = shipCost;
    }

    public LocalDateTime getShipDateExpected() {
        return shipDateExpected;
    }

    public void setShipDateExpected(LocalDateTime shipDateExpected) {
        this.shipDateExpected = shipDateExpected;
    }

    public LocalDateTime getShipDateActual() {
        return shipDateActual;
    }

    public void setShipDateActual(LocalDateTime shipDateActual) {
        this.shipDateActual = shipDateActual;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Integer getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(Integer shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public String getShippingStatusDescription() {
        return shippingStatusDescription;
    }

    public void setShippingStatusDescription(String shippingStatusDescription) {
        this.shippingStatusDescription = shippingStatusDescription;
    }
}