package com.bbbrewery.backend.controller;

import com.bbbrewery.backend.model.Shipping;
import com.bbbrewery.backend.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"}, allowCredentials = "false")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    // ================== CRUD Operations ==================

    /**
     * Obtenir tous les tarifs d'expédition
     */
    @GetMapping("/rates")
    public ResponseEntity<List<Shipping>> getAllShippingRates() {
        List<Shipping> rates = shippingService.getAllShippingRates();
        return ResponseEntity.ok(rates);
    }

    /**
     * Obtenir un tarif d'expédition par ID
     */
    @GetMapping("/rates/{id}")
    public ResponseEntity<Shipping> getShippingRateById(@PathVariable Long id) {
        Optional<Shipping> rate = shippingService.getShippingRateById(id);
        return rate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Créer un nouveau tarif d'expédition
     */
    @PostMapping("/rates")
    public ResponseEntity<Shipping> createShippingRate(@RequestBody Shipping shipping) {
        try {
            // Validation de la plage de poids
            if (!shippingService.validateWeightRange(shipping.getLow(), shipping.getHigh())) {
                return ResponseEntity.badRequest().build();
            }

            Shipping savedRate = shippingService.saveShippingRate(shipping);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mettre à jour un tarif d'expédition
     */
    @PutMapping("/rates/{id}")
    public ResponseEntity<Shipping> updateShippingRate(@PathVariable Long id, @RequestBody Shipping shipping) {
        try {
            Shipping updatedRate = shippingService.updateShippingRate(id, shipping);
            return ResponseEntity.ok(updatedRate);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer un tarif d'expédition
     */
    @DeleteMapping("/rates/{id}")
    public ResponseEntity<Void> deleteShippingRate(@PathVariable Long id) {
        try {
            shippingService.deleteShippingRate(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== Business Logic Endpoints ==================

    /**
     * Calculer le coût d'expédition pour un poids et une méthode
     */
    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculateShippingCost(
            @RequestParam Integer weight,
            @RequestParam(defaultValue = "standard") String method) {

        if (weight == null || weight <= 0) {
            return ResponseEntity.badRequest().build();
        }

        BigDecimal cost = shippingService.calculateShippingCost(weight, method);
        return ResponseEntity.ok(cost);
    }

    /**
     * Obtenir tous les tarifs applicables pour un poids donné
     */
    @GetMapping("/applicable")
    public ResponseEntity<List<Shipping>> getApplicableRates(@RequestParam Integer weight) {
        if (weight == null || weight <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<Shipping> rates = shippingService.getApplicableRates(weight);
        return ResponseEntity.ok(rates);
    }

    /**
     * Obtenir toutes les méthodes d'expédition disponibles
     */
    @GetMapping("/methods")
    public ResponseEntity<List<String>> getAvailableShippingMethods() {
        List<String> methods = shippingService.getAvailableShippingMethods();
        return ResponseEntity.ok(methods);
    }

    /**
     * Obtenir les tarifs par méthode d'expédition
     */
    @GetMapping("/rates/method/{method}")
    public ResponseEntity<List<Shipping>> getRatesByMethod(@PathVariable String method) {
        List<Shipping> rates = shippingService.getRatesByMethod(method);
        return ResponseEntity.ok(rates);
    }

    /**
     * Obtenir les tarifs ordonnés par poids
     */
    @GetMapping("/rates/ordered-by-weight")
    public ResponseEntity<List<Shipping>> getRatesOrderedByWeight() {
        List<Shipping> rates = shippingService.getRatesOrderedByWeight();
        return ResponseEntity.ok(rates);
    }

    /**
     * Obtenir les tarifs ordonnés par prix
     */
    @GetMapping("/rates/ordered-by-price")
    public ResponseEntity<List<Shipping>> getRatesOrderedByPrice() {
        List<Shipping> rates = shippingService.getRatesOrderedByPrice();
        return ResponseEntity.ok(rates);
    }

    /**
     * Filtrer les tarifs par plage de prix
     */
    @GetMapping("/rates/price-range")
    public ResponseEntity<List<Shipping>> getRatesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        if (minPrice.compareTo(maxPrice) > 0) {
            return ResponseEntity.badRequest().build();
        }

        List<Shipping> rates = shippingService.getRatesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(rates);
    }

    // ================== Tracking & Status Endpoints ==================

    /**
     * Rechercher par numéro de suivi
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Shipping> findByTrackingNumber(@PathVariable String trackingNumber) {
        Optional<Shipping> shipping = shippingService.findByTrackingNumber(trackingNumber);
        return shipping.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mettre à jour le statut d'expédition
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Shipping> updateShippingStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {

        if (status < 1 || status > 6) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Shipping updatedShipping = shippingService.updateShippingStatus(id, status);
            return ResponseEntity.ok(updatedShipping);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marquer comme expédié
     */
    @PatchMapping("/{id}/ship")
    public ResponseEntity<Shipping> markAsShipped(@PathVariable Long id) {
        try {
            Shipping shippedItem = shippingService.markAsShipped(id);
            return ResponseEntity.ok(shippedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marquer comme livré
     */
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<Shipping> markAsDelivered(@PathVariable Long id) {
        try {
            Shipping deliveredItem = shippingService.markAsDelivered(id);
            return ResponseEntity.ok(deliveredItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtenir toutes les expéditions actives
     */
    @GetMapping("/active")
    public ResponseEntity<List<Shipping>> getActiveShipments() {
        List<Shipping> activeShipments = shippingService.getActiveShipments();
        return ResponseEntity.ok(activeShipments);
    }

    // ================== Validation Endpoints ==================

    /**
     * Valider une plage de poids
     */
    @GetMapping("/validate-range")
    public ResponseEntity<Boolean> validateWeightRange(
            @RequestParam Integer low,
            @RequestParam Integer high) {

        boolean isValid = shippingService.validateWeightRange(low, high);
        return ResponseEntity.ok(isValid);
    }
}