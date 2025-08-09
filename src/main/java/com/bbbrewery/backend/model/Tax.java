package com.bbbrewery.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "BB_TAX")
public class Tax {

    @Id
    @Column(name = "IDSTATE")
    private Long id;

    @Column(name = "STATE", length = 2)
    private String state;

    @NotNull
    @DecimalMin(value = "0.00", message = "Le taux de taxe ne peut pas être négatif")
    @DecimalMax(value = "100.00", message = "Le taux de taxe ne peut pas dépasser 100%")
    @Column(name = "TAXRATE", precision = 4, scale = 3, nullable = false)
    private BigDecimal taxRate;

    @Column(name = "ACTIVE")
    private Integer active;

    @Column(name = "DTCREATED")
    private LocalDateTime dateCreated;

    @Size(max = 100, message = "La description ne peut pas dépasser 100 caractères")
    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @Size(max = 15, message = "Le nom de province ne peut pas dépasser 15 caractères")
    @Column(name = "PROVINCE", length = 15)
    private String province;

    @DecimalMin(value = "0.00", message = "Le montant de taxe ne peut pas être négatif")
    @Column(name = "TAXAMOUNT", precision = 8, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "IDBASKET")
    private Long idBasket;

    // ========== CONSTRUCTEURS ==========
    public Tax() {
        this.dateCreated = LocalDateTime.now();
        this.active = 1; // 1 = actif, 0 = inactif
        this.taxRate = BigDecimal.ZERO;
    }

    /**
     * Constructeur pour une configuration de taux de taxe par état
     */
    public Tax(String state, BigDecimal taxRate) {
        this();
        this.state = state;
        this.taxRate = taxRate;
    }

    /**
     * Constructeur pour une configuration complète
     */
    public Tax(String state, BigDecimal taxRate, String province) {
        this();
        this.state = state;
        this.taxRate = taxRate;
        this.province = province;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Calcule le montant de taxe basé sur un sous-total
     */
    public void calculateTaxAmount(BigDecimal subtotal) {
        if (taxRate != null && subtotal != null) {
            this.taxAmount = subtotal
                    .multiply(taxRate)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }
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

    /**
     * Vérifie si la taxe est active
     */
    public boolean isActive() {
        return active != null && active == 1;
    }

    /**
     * Obtient la localisation complète
     */
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
     * Formate le taux de taxe pour l'affichage (en pourcentage)
     */
    public String getFormattedTaxRate() {
        if (taxRate == null) return "0.00%";
        // Multiplier par 100 car les taux sont stockés en décimal (0.045 = 4.5%)
        return taxRate.multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP) + "%";
    }

    /**
     * Formate le montant de taxe pour l'affichage
     */
    public String getFormattedTaxAmount() {
        if (taxAmount == null) return "$0.00";
        return "$" + taxAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valide la cohérence des données
     */
    public boolean isValid() {
        return id != null &&
                taxRate != null &&
                taxRate.compareTo(BigDecimal.ZERO) >= 0 &&
                state != null;
    }

    /**
     * Vérifie si cette taxe s'applique à un état donné
     */
    public boolean appliesTo(String stateCode) {
        return state != null && state.equalsIgnoreCase(stateCode);
    }

    /**
     * Active ou désactive cette configuration de taxe
     */
    public void setActiveStatus(boolean active) {
        this.active = active ? 1 : 0;
    }

    /**
     * Convertit le taux en pourcentage pour l'affichage
     */
    public BigDecimal getTaxRateAsPercentage() {
        if (taxRate == null) return BigDecimal.ZERO;
        return taxRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Définit le taux à partir d'un pourcentage
     */
    public void setTaxRateFromPercentage(BigDecimal percentage) {
        if (percentage != null) {
            this.taxRate = percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }
    }

    // ========== GETTERS ET SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Long getIdBasket() {
        return idBasket;
    }

    public void setIdBasket(Long idBasket) {
        this.idBasket = idBasket;
    }

    @Override
    public String toString() {
        return "Tax{" +
                "id=" + id +
                ", state='" + state + '\'' +
                ", taxRate=" + taxRate +
                ", taxAmount=" + taxAmount +
                ", province='" + province + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tax)) return false;
        Tax tax = (Tax) o;
        return id != null && id.equals(tax.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}