package com.bbbrewery.backend.controller;

import com.bbbrewery.backend.model.Tax;
import com.bbbrewery.backend.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tax")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"}, allowCredentials = "false")
public class TaxController {

    @Autowired
    private TaxService taxService;

    // ================== CRUD Operations ==================

    /**
     * Obtenir toutes les configurations de taxes
     */
    @GetMapping("/configurations")
    public ResponseEntity<List<Tax>> getAllTaxConfigurations() {
        List<Tax> taxes = taxService.getActiveTaxConfigurations();
        return ResponseEntity.ok(taxes);
    }

    /**
     * Obtenir une configuration de taxe par ID
     */
    @GetMapping("/configurations/{idState}")
    public ResponseEntity<Tax> getTaxConfigurationById(@PathVariable Long idState) {
        Optional<Tax> tax = taxService.getTaxById(idState);
        return tax.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Créer une nouvelle configuration de taxe
     */
    @PostMapping("/configurations")
    public ResponseEntity<Tax> createTaxConfiguration(@RequestBody Tax tax) {
        try {
            if (!taxService.validateTaxConfiguration(tax)) {
                return ResponseEntity.badRequest().build();
            }

            Tax savedTax = taxService.saveTax(tax);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTax);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mettre à jour une configuration de taxe
     */
    @PutMapping("/configurations/{idState}")
    public ResponseEntity<Tax> updateTaxConfiguration(@PathVariable Long idState, @RequestBody Tax tax) {
        try {
            Tax updatedTax = taxService.updateTax(idState, tax);
            return ResponseEntity.ok(updatedTax);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer une configuration de taxe
     */
    @DeleteMapping("/configurations/{idState}")
    public ResponseEntity<Void> deleteTaxConfiguration(@PathVariable Long idState) {
        try {
            taxService.deleteTax(idState);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== Tax Configuration Management ==================

    /**
     * Créer ou mettre à jour une configuration de taxe pour un état
     */
    @PostMapping("/configurations/state/{state}")
    public ResponseEntity<Tax> createOrUpdateStateConfiguration(
            @PathVariable String state,
            @RequestParam BigDecimal taxRatePercentage,
            @RequestParam(required = false) String description) {

        if (!taxService.isValidTaxRate(taxRatePercentage)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Tax tax = taxService.createOrUpdateTaxConfiguration(state, taxRatePercentage, description);
            return ResponseEntity.ok(tax);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtenir la configuration de taxe pour un état
     */
    @GetMapping("/configurations/state/{state}")
    public ResponseEntity<Tax> getTaxConfigurationByState(@PathVariable String state) {
        Optional<Tax> tax = taxService.getTaxConfigurationByState(state);
        return tax.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Activer/désactiver une configuration de taxe
     */
    @PatchMapping("/configurations/{idState}/toggle")
    public ResponseEntity<Tax> toggleTaxConfiguration(@PathVariable Long idState, @RequestParam boolean active) {
        try {
            Tax updatedTax = taxService.toggleTaxConfiguration(idState, active);
            return ResponseEntity.ok(updatedTax);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== Tax Calculations ==================

    /**
     * Calculer la taxe pour un montant et un état
     */
    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculateTax(
            @RequestParam BigDecimal amount,
            @RequestParam String state) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }

        BigDecimal taxAmount = taxService.calculateTax(amount, state);
        return ResponseEntity.ok(taxAmount);
    }

    /**
     * Calculer le montant total avec taxe
     */
    @GetMapping("/calculate-total")
    public ResponseEntity<CalculationResult> calculateTotalWithTax(
            @RequestParam BigDecimal amount,
            @RequestParam String state) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }

        BigDecimal taxAmount = taxService.calculateTax(amount, state);
        BigDecimal total = taxService.calculateTotalWithTax(amount, state);
        BigDecimal taxRate = taxService.getTaxRateForState(state);

        CalculationResult result = new CalculationResult(amount, taxAmount, total, taxRate, state);
        return ResponseEntity.ok(result);
    }

    /**
     * Obtenir le taux de taxe pour un état
     */
    @GetMapping("/rate/{state}")
    public ResponseEntity<BigDecimal> getTaxRateForState(@PathVariable String state) {
        BigDecimal taxRate = taxService.getTaxRateForState(state);
        return ResponseEntity.ok(taxRate);
    }

    // ================== Applied Taxes (Basket Integration) ==================

    /**
     * Appliquer une taxe à un panier
     */
    @PostMapping("/apply")
    public ResponseEntity<Tax> applyTaxToBasket(
            @RequestParam Long basketId,
            @RequestParam String state,
            @RequestParam BigDecimal subtotal) {

        try {
            Tax appliedTax = taxService.applyTaxToBasket(basketId, state, subtotal);
            return ResponseEntity.status(HttpStatus.CREATED).body(appliedTax);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtenir toutes les taxes appliquées à un panier
     */
    @GetMapping("/basket/{basketId}")
    public ResponseEntity<List<Tax>> getAppliedTaxesForBasket(@PathVariable Long basketId) {
        List<Tax> appliedTaxes = taxService.getAppliedTaxesForBasket(basketId);
        return ResponseEntity.ok(appliedTaxes);
    }

    /**
     * Obtenir le montant total des taxes pour un panier
     */
    @GetMapping("/basket/{basketId}/total")
    public ResponseEntity<BigDecimal> getTotalTaxAmountForBasket(@PathVariable Long basketId) {
        BigDecimal totalTax = taxService.getTotalTaxAmountForBasket(basketId);
        return ResponseEntity.ok(totalTax);
    }

    /**
     * Supprimer toutes les taxes appliquées à un panier
     */
    @DeleteMapping("/basket/{basketId}")
    public ResponseEntity<Void> removeAppliedTaxesForBasket(@PathVariable Long basketId) {
        try {
            taxService.removeAppliedTaxesForBasket(basketId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================== Queries & Information ==================

    /**
     * Obtenir tous les états avec des configurations de taxes
     */
    @GetMapping("/states")
    public ResponseEntity<List<String>> getStatesWithTaxConfigurations() {
        List<String> states = taxService.getStatesWithTaxConfigurations();
        return ResponseEntity.ok(states);
    }

    /**
     * Obtenir les taxes ordonnées par taux
     */
    @GetMapping("/configurations/ordered-by-rate")
    public ResponseEntity<List<Tax>> getTaxesOrderedByRate() {
        List<Tax> taxes = taxService.getTaxesOrderedByRate();
        return ResponseEntity.ok(taxes);
    }

    /**
     * Filtrer les taxes par plage de taux
     */
    @GetMapping("/configurations/rate-range")
    public ResponseEntity<List<Tax>> getTaxesByRateRange(
            @RequestParam BigDecimal minPercentage,
            @RequestParam BigDecimal maxPercentage) {

        if (minPercentage.compareTo(maxPercentage) > 0) {
            return ResponseEntity.badRequest().build();
        }

        List<Tax> taxes = taxService.getTaxesByRateRange(minPercentage, maxPercentage);
        return ResponseEntity.ok(taxes);
    }

    /**
     * Obtenir des statistiques sur les taxes
     */
    @GetMapping("/statistics")
    public ResponseEntity<TaxService.TaxStatistics> getTaxStatistics() {
        TaxService.TaxStatistics stats = taxService.getTaxStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Rechercher par description
     */
    @GetMapping("/search")
    public ResponseEntity<List<Tax>> searchByDescription(@RequestParam String keyword) {
        List<Tax> taxes = taxService.searchByDescription(keyword);
        return ResponseEntity.ok(taxes);
    }

    // ================== Validation Endpoints ==================

    /**
     * Vérifier si un état a une configuration de taxe
     */
    @GetMapping("/configurations/exists/{state}")
    public ResponseEntity<Boolean> hasConfigurationForState(@PathVariable String state) {
        boolean exists = taxService.hasConfigurationForState(state);
        return ResponseEntity.ok(exists);
    }

    /**
     * Valider un taux de taxe
     */
    @GetMapping("/validate-rate")
    public ResponseEntity<Boolean> validateTaxRate(@RequestParam BigDecimal percentage) {
        boolean isValid = taxService.isValidTaxRate(percentage);
        return ResponseEntity.ok(isValid);
    }

    // ================== Inner Classes ==================

    /**
     * Classe pour retourner le résultat complet d'un calcul de taxe
     */
    public static class CalculationResult {
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal total;
        private BigDecimal taxRate;
        private String state;

        public CalculationResult(BigDecimal subtotal, BigDecimal taxAmount, BigDecimal total, BigDecimal taxRate, String state) {
            this.subtotal = subtotal;
            this.taxAmount = taxAmount;
            this.total = total;
            this.taxRate = taxRate;
            this.state = state;
        }

        // Getters
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public BigDecimal getTotal() { return total; }
        public BigDecimal getTaxRate() { return taxRate; }
        public String getState() { return state; }

        // Setters
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
        public void setState(String state) { this.state = state; }

        @Override
        public String toString() {
            return "CalculationResult{" +
                    "subtotal=" + subtotal +
                    ", taxAmount=" + taxAmount +
                    ", total=" + total +
                    ", taxRate=" + taxRate + "%" +
                    ", state='" + state + '\'' +
                    '}';
        }
    }
}