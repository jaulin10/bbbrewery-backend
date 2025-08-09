package com.bbbrewery.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BB_SHIPPING")
public class Shipping {

    @Id
    @Column(name = "IDRANGE")
    private Long idRange;

    @Column(name = "LOW")
    private Integer low;

    @Column(name = "HIGH")
    private Integer high;

    @Column(name = "FEE")
    private BigDecimal fee;

    @Column(name = "DTCREATED")
    private LocalDateTime dateCreated;

    @Column(name = "SHIPCOST", precision = 8, scale = 2)
    private BigDecimal shipCost;

    @Column(name = "SHIPDATEACTUAL")
    private LocalDateTime shipDateActual;

    @Column(name = "SHIPDATEEXPECTED")
    private LocalDateTime shipDateExpected;

    @Column(name = "SHIPPINGMETHOD", length = 50)
    private String shippingMethod;

    @Column(name = "SHIPPINGSTATUS")
    private Integer shippingStatus;

    @Column(name = "TRACKINGNUMBER", length = 50)
    private String trackingNumber;

    // Constructeurs
    public Shipping() {
        this.dateCreated = LocalDateTime.now();
        this.shippingStatus = 1; // 1 = Pending
    }

    public Shipping(Integer low, Integer high, BigDecimal fee, String shippingMethod) {
        this();
        this.low = low;
        this.high = high;
        this.fee = fee;
        this.shippingMethod = shippingMethod;
    }

    // Méthodes utilitaires
    public String getShippingStatusDescription() {
        return switch (shippingStatus != null ? shippingStatus : 0) {
            case 1 -> "Pending";
            case 2 -> "Processing";
            case 3 -> "Shipped";
            case 4 -> "In Transit";
            case 5 -> "Delivered";
            case 6 -> "Cancelled";
            default -> "Unknown";
        };
    }

    public boolean isShipped() {
        return shippingStatus != null && shippingStatus >= 3;
    }

    public boolean isDelivered() {
        return shippingStatus != null && shippingStatus == 5;
    }

    public void markAsShipped() {
        this.shippingStatus = 3;
        this.shipDateActual = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.shippingStatus = 5;
    }

    public int calculateEstimatedDeliveryDays() {
        if (shippingMethod == null) return 7; // Default

        return switch (shippingMethod.toLowerCase()) {
            case "standard" -> 7;
            case "express" -> 3;
            case "overnight" -> 1;
            case "priority" -> 2;
            default -> 5;
        };
    }

    // Méthode pour vérifier si un poids est dans cette plage
    public boolean isWeightInRange(int weight) {
        return weight >= (low != null ? low : 0) && weight <= (high != null ? high : Integer.MAX_VALUE);
    }

    // Getters et Setters
    public Long getIdRange() {
        return idRange;
    }

    public void setIdRange(Long idRange) {
        this.idRange = idRange;
    }

    public Integer getLow() {
        return low;
    }

    public void setLow(Integer low) {
        this.low = low;
    }

    public Integer getHigh() {
        return high;
    }

    public void setHigh(Integer high) {
        this.high = high;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
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

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "Shipping{" +
                "idRange=" + idRange +
                ", low=" + low +
                ", high=" + high +
                ", fee=" + fee +
                ", shippingMethod='" + shippingMethod + '\'' +
                ", shippingStatus=" + shippingStatus +
                '}';
    }
}