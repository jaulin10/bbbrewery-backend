package com.bbbrewery.backend.service;

import com.bbbrewery.backend.model.Shipping;
import com.bbbrewery.backend.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShippingService {

    @Autowired
    private ShippingRepository shippingRepository;

    // ================== CRUD Operations ==================

    public List<Shipping> getAllShippingRates() {
        return shippingRepository.findAll();
    }

    public Optional<Shipping> getShippingRateById(Long id) {
        return shippingRepository.findById(id);
    }

    public Shipping saveShippingRate(Shipping shipping) {
        if (shipping.getDateCreated() == null) {
            shipping.setDateCreated(LocalDateTime.now());
        }
        return shippingRepository.save(shipping);
    }

    public void deleteShippingRate(Long id) {
        shippingRepository.deleteById(id);
    }

    public Shipping updateShippingRate(Long id, Shipping updatedShipping) {
        return shippingRepository.findById(id)
                .map(shipping -> {
                    shipping.setLow(updatedShipping.getLow());
                    shipping.setHigh(updatedShipping.getHigh());
                    shipping.setFee(updatedShipping.getFee());
                    shipping.setShippingMethod(updatedShipping.getShippingMethod());
                    shipping.setShipCost(updatedShipping.getShipCost());
                    shipping.setShippingStatus(updatedShipping.getShippingStatus());
                    shipping.setTrackingNumber(updatedShipping.getTrackingNumber());
                    shipping.setShipDateExpected(updatedShipping.getShipDateExpected());
                    shipping.setShipDateActual(updatedShipping.getShipDateActual());
                    return shippingRepository.save(shipping);
                })
                .orElseThrow(() -> new RuntimeException("Shipping rate not found with id: " + id));
    }

    // ================== Business Logic ==================

    /**
     * Calcule le coût d'expédition pour un poids et une méthode donnés
     */
    public BigDecimal calculateShippingCost(Integer weight, String method) {
        Optional<Shipping> shippingRate = shippingRepository.findByWeightRangeAndMethod(weight, method);

        if (shippingRate.isPresent()) {
            Shipping rate = shippingRate.get();
            // Priorité au shipCost, sinon utiliser fee
            return rate.getShipCost() != null ? rate.getShipCost() : rate.getFee();
        }

        // Si aucun tarif trouvé pour la méthode spécifique, chercher dans toutes les méthodes
        List<Shipping> rates = shippingRepository.findByWeightRange(weight);
        if (!rates.isEmpty()) {
            Shipping rate = rates.get(0); // Prendre le premier trouvé
            return rate.getShipCost() != null ? rate.getShipCost() : rate.getFee();
        }

        // Retourner un tarif par défaut si rien n'est trouvé
        return getDefaultShippingCost(method);
    }

    /**
     * Trouve tous les tarifs applicables pour un poids donné
     */
    public List<Shipping> getApplicableRates(Integer weight) {
        return shippingRepository.findByWeightRange(weight);
    }

    /**
     * Obtient toutes les méthodes d'expédition disponibles
     */
    public List<String> getAvailableShippingMethods() {
        return shippingRepository.findAllShippingMethods();
    }

    /**
     * Trouve les tarifs par méthode d'expédition
     */
    public List<Shipping> getRatesByMethod(String method) {
        return shippingRepository.findByShippingMethodIgnoreCase(method);
    }

    /**
     * Trouve les tarifs ordonnés par poids
     */
    public List<Shipping> getRatesOrderedByWeight() {
        return shippingRepository.findAllOrderedByLowWeight();
    }

    /**
     * Trouve les tarifs ordonnés par prix
     */
    public List<Shipping> getRatesOrderedByPrice() {
        return shippingRepository.findAllOrderedByFee();
    }

    /**
     * Valide qu'une nouvelle plage de poids ne chevauche pas avec les existantes
     */
    public boolean validateWeightRange(Integer low, Integer high) {
        if (low == null || high == null || low >= high) {
            return false;
        }
        return !shippingRepository.existsOverlappingRange(low, high);
    }

    /**
     * Trouve un tarif par numéro de suivi
     */
    public Optional<Shipping> findByTrackingNumber(String trackingNumber) {
        return shippingRepository.findByTrackingNumber(trackingNumber);
    }

    /**
     * Met à jour le statut d'expédition
     */
    public Shipping updateShippingStatus(Long id, Integer newStatus) {
        return shippingRepository.findById(id)
                .map(shipping -> {
                    shipping.setShippingStatus(newStatus);
                    if (newStatus == 3) { // Shipped
                        shipping.setShipDateActual(LocalDateTime.now());
                    }
                    return shippingRepository.save(shipping);
                })
                .orElseThrow(() -> new RuntimeException("Shipping not found with id: " + id));
    }

    /**
     * Marque une expédition comme livrée
     */
    public Shipping markAsDelivered(Long id) {
        return updateShippingStatus(id, 5);
    }

    /**
     * Marque une expédition comme expédiée
     */
    public Shipping markAsShipped(Long id) {
        return updateShippingStatus(id, 3);
    }

    /**
     * Obtient toutes les expéditions actives
     */
    public List<Shipping> getActiveShipments() {
        return shippingRepository.findActiveShipments();
    }

    /**
     * Filtre les tarifs par plage de prix
     */
    public List<Shipping> getRatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return shippingRepository.findByFeeBetween(minPrice, maxPrice);
    }

    // ================== Private Helper Methods ==================

    private BigDecimal getDefaultShippingCost(String method) {
        // Tarifs par défaut basés sur la méthode
        return switch (method != null ? method.toLowerCase() : "standard") {
            case "express" -> new BigDecimal("15.00");
            case "overnight" -> new BigDecimal("25.00");
            case "priority" -> new BigDecimal("12.00");
            default -> new BigDecimal("8.00"); // standard
        };
    }
}