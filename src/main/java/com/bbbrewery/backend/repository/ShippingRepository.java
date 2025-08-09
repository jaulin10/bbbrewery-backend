package com.bbbrewery.backend.repository;

import com.bbbrewery.backend.model.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {

    // Recherche par méthode d'expédition
    List<Shipping> findByShippingMethod(String shippingMethod);

    // Recherche par méthode d'expédition (ignorer la casse)
    List<Shipping> findByShippingMethodIgnoreCase(String shippingMethod);

    // Trouver les tarifs pour un poids donné
    @Query("SELECT s FROM Shipping s WHERE :weight >= s.low AND :weight <= s.high")
    List<Shipping> findByWeightRange(@Param("weight") Integer weight);

    // Trouver le tarif pour un poids et une méthode spécifiques
    @Query("SELECT s FROM Shipping s WHERE :weight >= s.low AND :weight <= s.high AND s.shippingMethod = :method")
    Optional<Shipping> findByWeightRangeAndMethod(@Param("weight") Integer weight, @Param("method") String method);

    // Recherche par plage de frais
    List<Shipping> findByFeeBetween(BigDecimal minFee, BigDecimal maxFee);

    // Recherche par plage de coûts d'expédition
    List<Shipping> findByShipCostBetween(BigDecimal minCost, BigDecimal maxCost);

    // Trouver par statut d'expédition
    List<Shipping> findByShippingStatus(Integer status);

    // Trouver les expéditions actives (statut 1-5, pas annulées)
    @Query("SELECT s FROM Shipping s WHERE s.shippingStatus BETWEEN 1 AND 5")
    List<Shipping> findActiveShipments();

    // Trouver par plage de poids (low-high)
    @Query("SELECT s FROM Shipping s WHERE s.low >= :minWeight AND s.high <= :maxWeight")
    List<Shipping> findByWeightRangeBounds(@Param("minWeight") Integer minWeight, @Param("maxWeight") Integer maxWeight);

    // Recherche par numéro de suivi
    Optional<Shipping> findByTrackingNumber(String trackingNumber);

    // Trouver tous les tarifs ordonnés par poids minimum
    @Query("SELECT s FROM Shipping s ORDER BY s.low ASC")
    List<Shipping> findAllOrderedByLowWeight();

    // Trouver tous les tarifs ordonnés par frais
    @Query("SELECT s FROM Shipping s ORDER BY s.fee ASC")
    List<Shipping> findAllOrderedByFee();

    // Recherche des méthodes d'expédition disponibles
    @Query("SELECT DISTINCT s.shippingMethod FROM Shipping s WHERE s.shippingMethod IS NOT NULL")
    List<String> findAllShippingMethods();

    // Vérifier si une plage de poids existe déjà
    @Query("SELECT COUNT(s) > 0 FROM Shipping s WHERE " +
            "(:low BETWEEN s.low AND s.high) OR (:high BETWEEN s.low AND s.high) OR " +
            "(s.low BETWEEN :low AND :high) OR (s.high BETWEEN :low AND :high)")
    boolean existsOverlappingRange(@Param("low") Integer low, @Param("high") Integer high);

    // Trouver les tarifs avec un poids maximum supérieur à une valeur
    List<Shipping> findByHighGreaterThan(Integer weight);

    // Trouver les tarifs avec un poids minimum inférieur à une valeur
    List<Shipping> findByLowLessThan(Integer weight);
}