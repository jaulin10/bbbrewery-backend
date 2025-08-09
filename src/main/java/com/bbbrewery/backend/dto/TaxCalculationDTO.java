package com.bbbrewery.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO pour les calculs de taxe adapté à la structure BB_TAX
 */
public class TaxCalculationDTO {

    @NotNull
    @DecimalMin(value = "0.0", message = "Le sous-total ne peut pas être négatif")
    private BigDecimal subtotal;

    private BigDecimal originalSubtotal;

    @Size(max = 2, message = "Le code d'état ne peut pas dépasser 2 caractères")
    private String state;

    @Size(max = 15, message = "Le nom de province ne peut pas dépasser 15 caractères")
    private String province;

    // Taux de taxe en format décimal (0.045 pour 4.5%)
    @DecimalMin(value = "0.0", message = "Le taux de taxe ne peut pas être négatif")
    private BigDecimal taxRate;

    // Taux de taxe en format pourcentage pour l'affichage (4.5 pour 4.5%)
    @DecimalMin(value = "0.0", message = "Le taux de taxe ne peut pas être négatif")
    private BigDecimal taxRatePercentage;

    @DecimalMin(value = "0.0", message = "Le montant de taxe ne peut pas être négatif")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0", message = "Le total ne peut pas être négatif")
    private BigDecimal total;

    private BigDecimal discountAmount;

    private String description;

    // ID de la configuration de taxe utilisée
    private Long id;

    // ID du panier si la taxe est appliquée
    private Long basketId;

    // Indique si c'est une configuration ou une taxe appliquée
    private boolean isAppliedTax;

    // Constructeurs
    public TaxCalculationDTO() {
        this.taxRate = BigDecimal.ZERO;
        this.taxRatePercentage = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.isAppliedTax = false;
    }

    /**
     * Constructeur pour un calcul de taxe simple
     */
    public TaxCalculationDTO(BigDecimal subtotal, String state, BigDecimal taxRateDecimal, BigDecimal taxAmount) {
        this();
        this.subtotal = subtotal;
        this.state = state;
        this.taxRate = taxRateDecimal;
        this.taxRatePercentage = convertToPercentage(taxRateDecimal);
        this.taxAmount = taxAmount;
        this.total = subtotal.add(taxAmount);
    }

    /**
     * Constructeur complet avec panier
     */
    public TaxCalculationDTO(BigDecimal subtotal, String state, BigDecimal taxRateDecimal,
                             BigDecimal taxAmount, Long basketId, Long id) {
        this(subtotal, state, taxRateDecimal, taxAmount);
        this.basketId = basketId;
        this.id = id;
        this.isAppliedTax = basketId != null;
    }

    // ================== Méthodes utilitaires ==================

    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasTax() {
        return taxAmount != null && taxAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isAppliedToBasket() {
        return basketId != null;
    }

    public boolean isConfiguration() {
        return basketId == null;
    }

    public BigDecimal getEffectiveSubtotal() {
        if (hasDiscount() && originalSubtotal != null) {
            return originalSubtotal.subtract(discountAmount);
        }
        return subtotal;
    }

    public BigDecimal getSavingsFromDiscount() {
        if (hasDiscount() && originalSubtotal != null) {
            return discountAmount;
        }
        return BigDecimal.ZERO;
    }

    /**
     * Formate le taux de taxe pour l'affichage
     */
    public String getFormattedTaxRate() {
        if (taxRatePercentage == null) return "0.00%";
        return taxRatePercentage.setScale(2, RoundingMode.HALF_UP) + "%";
    }

    /**
     * Obtient la description du type de taxe basée sur l'état
     */
    public String getTaxTypeDescription() {
        if (state == null) return "Unknown Tax";

        return switch (state.toUpperCase()) {
            case "VA" -> "Virginia Sales Tax";
            case "NC" -> "North Carolina Sales Tax";
            case "SC" -> "South Carolina Sales Tax";
            case "CA" -> "California Sales Tax";
            case "NY" -> "New York Sales Tax";
            case "TX" -> "Texas Sales Tax";
            case "FL" -> "Florida Sales Tax";
            default -> state + " Sales Tax";
        };
    }

    public String getLocationDescription() {
        StringBuilder location = new StringBuilder();
        if (state != null && !state.trim().isEmpty()) {
            location.append(state);
        }
        if (province != null && !province.trim().isEmpty()) {
            if (location.length() > 0) {
                location.append(", ");
            }
            location.append(province);
        }
        return location.toString();
    }

    /**
     * Calcule le montant de taxe basé sur le sous-total et le taux
     */
    public void calculateTaxAmount() {
        if (taxRate != null && subtotal != null) {
            this.taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            this.total = subtotal.add(taxAmount);
        }
    }

    /**
     * Recalcule le total en tenant compte des remises
     */
    public void recalculateTotal() {
        BigDecimal effectiveSubtotal = getEffectiveSubtotal();
        if (taxRate != null && effectiveSubtotal != null) {
            this.taxAmount = effectiveSubtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            this.total = effectiveSubtotal.add(taxAmount);
        }
    }

    /**
     * Formate le montant de taxe pour l'affichage
     */
    public String getFormattedTaxAmount() {
        if (taxAmount == null) return "$0.00";
        return "$" + taxAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formate le total pour l'affichage
     */
    public String getFormattedTotal() {
        if (total == null) return "$0.00";
        return "$" + total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formate le sous-total pour l'affichage
     */
    public String getFormattedSubtotal() {
        if (subtotal == null) return "$0.00";
        return "$" + subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valide la cohérence des données
     */
    public boolean isValid() {
        return subtotal != null &&
                subtotal.compareTo(BigDecimal.ZERO) >= 0 &&
                taxRate != null &&
                taxRate.compareTo(BigDecimal.ZERO) >= 0 &&
                state != null;
    }

    // ================== Méthodes de conversion ==================

    /**
     * Convertit un taux décimal en pourcentage
     */
    private BigDecimal convertToPercentage(BigDecimal decimalRate) {
        if (decimalRate == null) return BigDecimal.ZERO;
        return decimalRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convertit un pourcentage en taux décimal
     */
    private BigDecimal convertToDecimal(BigDecimal percentage) {
        if (percentage == null) return BigDecimal.ZERO;
        return percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    /**
     * Met à jour le taux à partir d'un pourcentage
     */
    public void setTaxRateFromPercentage(BigDecimal percentage) {
        this.taxRatePercentage = percentage;
        this.taxRate = convertToDecimal(percentage);
    }

    /**
     * Met à jour le pourcentage à partir du taux décimal
     */
    public void updatePercentageFromRate() {
        this.taxRatePercentage = convertToPercentage(this.taxRate);
    }

    // ================== Getters et Setters ==================

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getOriginalSubtotal() {
        return originalSubtotal;
    }

    public void setOriginalSubtotal(BigDecimal originalSubtotal) {
        this.originalSubtotal = originalSubtotal;
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

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
        updatePercentageFromRate();
    }

    public BigDecimal getTaxRatePercentage() {
        return taxRatePercentage;
    }

    public void setTaxRatePercentage(BigDecimal taxRatePercentage) {
        this.taxRatePercentage = taxRatePercentage;
        this.taxRate = convertToDecimal(taxRatePercentage);
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBasketId() {
        return basketId;
    }

    public void setBasketId(Long basketId) {
        this.basketId = basketId;
        this.isAppliedTax = basketId != null;
    }

    public boolean isAppliedTax() {
        return isAppliedTax;
    }

    public void setAppliedTax(boolean appliedTax) {
        isAppliedTax = appliedTax;
    }

    @Override
    public String toString() {
        return "TaxCalculationDTO{" +
                "subtotal=" + subtotal +
                ", state='" + state + '\'' +
                ", province='" + province + '\'' +
                ", taxRate=" + taxRate +
                ", taxRatePercentage=" + taxRatePercentage + "%" +
                ", taxAmount=" + taxAmount +
                ", total=" + total +
                ", basketId=" + basketId +
                ", isAppliedTax=" + isAppliedTax +
                ", hasDiscount=" + hasDiscount() +
                '}';
    }
}