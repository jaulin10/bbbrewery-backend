package com.bbbrewery.backend.service;

import com.bbbrewery.backend.model.Tax;
import com.bbbrewery.backend.repository.TaxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaxService {

    @Autowired
    private TaxRepository taxRepository;

    // ================== CRUD Operations ==================

    public List<Tax> getAllTaxes() {
        return taxRepository.findAll();
    }

    public Optional<Tax> getTaxById(Long idState) {
        return taxRepository.findById(idState);
    }

    public Tax saveTax(Tax tax) {
        if (tax.getDateCreated() == null) {
            tax.setDateCreated(LocalDateTime.now());
        }
        return taxRepository.save(tax);
    }

    public void deleteTax(Long idState) {
        taxRepository.deleteById(idState);
    }

    public Tax updateTax(Long idState, Tax updatedTax) {
        return taxRepository.findById(idState)
                .map(tax -> {
                    tax.setState(updatedTax.getState());
                    tax.setTaxRate(updatedTax.getTaxRate());
                    tax.setActive(updatedTax.getActive());
                    tax.setDescription(updatedTax.getDescription());
                    tax.setProvince(updatedTax.getProvince());
                    tax.setTaxAmount(updatedTax.getTaxAmount());
                    tax.setIdBasket(updatedTax.getIdBasket());
                    return taxRepository.save(tax);
                })
                .orElseThrow(() -> new RuntimeException("Tax configuration not found with id: " + idState));
    }

    // ================== Business Logic - Tax Configurations ==================

    /**
     * Obtient toutes les configurations de taxes actives
     */
    public List<Tax> getActiveTaxConfigurations() {
        return taxRepository.findAllActiveOrderedByState();
    }

    /**
     * Obtient la configuration de taxe pour un état
     */
    public Optional<Tax> getTaxConfigurationByState(String state) {
        return taxRepository.findActiveConfigurationByState(state.toUpperCase());
    }

    /**
     * Crée ou met à jour une configuration de taxe pour un état
     */
    public Tax createOrUpdateTaxConfiguration(String state, BigDecimal taxRatePercentage, String description) {
        String stateCode = state.toUpperCase();

        // Chercher une configuration existante pour cet état
        Optional<Tax> existingTax = taxRepository.findActiveConfigurationByState(stateCode);

        Tax tax;
        if (existingTax.isPresent()) {
            tax = existingTax.get();
        } else {
            tax = new Tax();
            tax.setState(stateCode);
        }

        tax.setTaxRateFromPercentage(taxRatePercentage);
        tax.setDescription(description);
        tax.setActive(1); // 1 = actif
        tax.setIdBasket(null); // Configuration, pas appliquée à un panier

        return taxRepository.save(tax);
    }

    /**
     * Active ou désactive une configuration de taxe
     */
    public Tax toggleTaxConfiguration(Long idState, boolean active) {
        return taxRepository.findById(idState)
                .map(tax -> {
                    tax.setActive(active ? 1 : 0);
                    return taxRepository.save(tax);
                })
                .orElseThrow(() -> new RuntimeException("Tax configuration not found with id: " + idState));
    }

    // ================== Business Logic - Tax Calculations ==================

    /**
     * Calcule la taxe pour un montant et un état donnés
     */
    public BigDecimal calculateTax(BigDecimal amount, String state) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        Optional<Tax> taxConfig = getTaxConfigurationByState(state);
        if (taxConfig.isPresent() && taxConfig.get().isActive()) {
            BigDecimal taxRate = taxConfig.get().getTaxRate();
            return amount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO; // Pas de taxe si configuration introuvable
    }

    /**
     * Calcule le montant total avec taxe
     */
    public BigDecimal calculateTotalWithTax(BigDecimal amount, String state) {
        BigDecimal taxAmount = calculateTax(amount, state);
        return amount.add(taxAmount);
    }

    /**
     * Obtient le taux de taxe pour un état (en pourcentage)
     */
    public BigDecimal getTaxRateForState(String state) {
        Optional<Tax> taxConfig = getTaxConfigurationByState(state);
        if (taxConfig.isPresent()) {
            return taxConfig.get().getTaxRateAsPercentage();
        }
        return BigDecimal.ZERO;
    }

    // ================== Business Logic - Applied Taxes ==================

    /**
     * Applique une taxe à un panier et sauvegarde le record
     */
    public Tax applyTaxToBasket(Long basketId, String state, BigDecimal subtotal) {
        Optional<Tax> configTax = getTaxConfigurationByState(state);

        if (configTax.isEmpty() || !configTax.get().isActive()) {
            throw new RuntimeException("No active tax configuration found for state: " + state);
        }

        Tax appliedTax = new Tax();
        appliedTax.setState(state.toUpperCase());
        appliedTax.setTaxRate(configTax.get().getTaxRate());
        appliedTax.setIdBasket(basketId);
        appliedTax.setActive(1); // 1 = actif
        appliedTax.calculateTaxAmount(subtotal);

        return taxRepository.save(appliedTax);
    }

    /**
     * Obtient toutes les taxes appliquées à un panier
     */
    public List<Tax> getAppliedTaxesForBasket(Long basketId) {
        return taxRepository.findByIdBasket(basketId);
    }

    /**
     * Calcule le montant total des taxes pour un panier
     */
    public BigDecimal getTotalTaxAmountForBasket(Long basketId) {
        BigDecimal total = taxRepository.getTotalTaxAmountForBasket(basketId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Supprime toutes les taxes appliquées à un panier
     */
    public void removeAppliedTaxesForBasket(Long basketId) {
        List<Tax> appliedTaxes = taxRepository.findByIdBasket(basketId);
        taxRepository.deleteAll(appliedTaxes);
    }

    // ================== Queries & Statistics ==================

    /**
     * Obtient tous les états avec des configurations de taxes
     */
    public List<String> getStatesWithTaxConfigurations() {
        return taxRepository.findAllStatesWithTaxConfigurations();
    }

    /**
     * Obtient les taxes par taux (croissant)
     */
    public List<Tax> getTaxesOrderedByRate() {
        return taxRepository.findAllOrderedByTaxRateAsc();
    }

    /**
     * Filtre les taxes par plage de taux
     */
    public List<Tax> getTaxesByRateRange(BigDecimal minPercentage, BigDecimal maxPercentage) {
        BigDecimal minRate = minPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal maxRate = maxPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return taxRepository.findByTaxRateBetween(minRate, maxRate);
    }

    /**
     * Obtient des statistiques sur les taux de taxes
     */
    public TaxStatistics getTaxStatistics() {
        BigDecimal avgRate = taxRepository.getAverageTaxRate();
        BigDecimal maxRate = taxRepository.getMaxTaxRate();
        BigDecimal minRate = taxRepository.getMinTaxRate();

        return new TaxStatistics(
                avgRate != null ? avgRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                maxRate != null ? maxRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                minRate != null ? minRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO
        );
    }

    /**
     * Recherche par mot-clé dans la description
     */
    public List<Tax> searchByDescription(String keyword) {
        return taxRepository.findByDescriptionContaining(keyword);
    }

    /**
     * Vérifie si un état a déjà une configuration
     */
    public boolean hasConfigurationForState(String state) {
        return taxRepository.existsConfigurationForState(state.toUpperCase());
    }

    // ================== Validation Methods ==================

    /**
     * Valide une configuration de taxe
     */
    public boolean validateTaxConfiguration(Tax tax) {
        return tax != null &&
                tax.getState() != null &&
                tax.getTaxRate() != null &&
                tax.getTaxRate().compareTo(BigDecimal.ZERO) >= 0 &&
                tax.getTaxRate().compareTo(new BigDecimal("1.00")) <= 0; // Max 100%
    }

    /**
     * Valide un taux de taxe en pourcentage
     */
    public boolean isValidTaxRate(BigDecimal percentage) {
        return percentage != null &&
                percentage.compareTo(BigDecimal.ZERO) >= 0 &&
                percentage.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    // ================== Helper Methods ==================
    // Supprimé les méthodes generateTaxId et generateAppliedTaxId car on utilise des IDs auto-générés

    // ================== Inner Class for Statistics ==================

    public static class TaxStatistics {
        private final BigDecimal averageRate;
        private final BigDecimal maxRate;
        private final BigDecimal minRate;

        public TaxStatistics(BigDecimal averageRate, BigDecimal maxRate, BigDecimal minRate) {
            this.averageRate = averageRate;
            this.maxRate = maxRate;
            this.minRate = minRate;
        }

        public BigDecimal getAverageRate() { return averageRate; }
        public BigDecimal getMaxRate() { return maxRate; }
        public BigDecimal getMinRate() { return minRate; }
    }
}