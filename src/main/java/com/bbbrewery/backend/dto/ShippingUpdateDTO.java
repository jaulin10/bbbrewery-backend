package com.bbbrewery.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShippingUpdateDTO {

    @DecimalMin(value = "0.0", message = "Le coût d'expédition ne peut pas être négatif")
    private BigDecimal shipCost;

    @Size(max = 50, message = "La méthode d'expédition ne peut pas dépasser 50 caractères")
    private String shippingMethod;

    @Size(max = 50, message = "Le numéro de suivi ne peut pas dépasser 50 caractères")
    private String trackingNumber;

    @Min(value = 1, message = "Le statut d'expédition doit être entre 1 et 6")
    @Max(value = 6, message = "Le statut d'expédition doit être entre 1 et 6")
    private Integer shippingStatus;

    private LocalDateTime shipDateExpected;

    private LocalDateTime shipDateActual;

    // Constructeurs
    public ShippingUpdateDTO() {
    }

    public ShippingUpdateDTO(BigDecimal shipCost, String shippingMethod, Integer shippingStatus) {
        this.shipCost = shipCost;
        this.shippingMethod = shippingMethod;
        this.shippingStatus = shippingStatus;
    }

    // Getters et Setters
    public BigDecimal getShipCost() {
        return shipCost;
    }

    public void setShipCost(BigDecimal shipCost) {
        this.shipCost = shipCost;
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

    @Override
    public String toString() {
        return "ShippingUpdateDTO{" +
                "shipCost=" + shipCost +
                ", shippingMethod='" + shippingMethod + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", shippingStatus=" + shippingStatus +
                ", shipDateExpected=" + shipDateExpected +
                ", shipDateActual=" + shipDateActual +
                '}';
    }
}