package com.bbbrewery.backend.repository;

import com.bbbrewery.backend.model.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {

    // ================== Recherche par État ==================

    /**
     * Trouve la configuration de taxe pour un état spécifique
     */
    Optional<Tax> findByState(String state);

    /**
     * Trouve la configuration de taxe pour un état (ignorer la casse)
     */
    Optional<Tax> findByStateIgnoreCase(String state);

    /**
     * Trouve toutes les taxes actives pour un état
     */
    @Query("SELECT t FROM Tax t WHERE t.state = :state AND t.active = 1")
    List<Tax> findActiveByState(@Param("state") String state);

    /**
     * Trouve toutes les configurations de taxes par liste d'états
     */
    List<Tax> findByStateIn(List<String> states);

    // ================== Recherche par Statut ==================

    /**
     * Trouve toutes les taxes actives
     */
    @Query("SELECT t FROM Tax t WHERE t.active = 1")
    List<Tax> findByActiveTrue();

    /**
     * Trouve toutes les taxes inactives
     */
    @Query("SELECT t FROM Tax t WHERE t.active = 0")
    List<Tax> findByActiveFalse();

    /**
     * Trouve toutes les taxes avec un statut spécifique
     */
    List<Tax> findByActive(Integer active);

    // ================== Recherche par Taux ==================

    /**
     * Trouve les taxes avec un taux supérieur à une valeur
     */
    List<Tax> findByTaxRateGreaterThan(BigDecimal rate);

    /**
     * Trouve les taxes avec un taux inférieur à une valeur
     */
    List<Tax> findByTaxRateLessThan(BigDecimal rate);

    /**
     * Trouve les taxes dans une plage de taux
     */
    List<Tax> findByTaxRateBetween(BigDecimal minRate, BigDecimal maxRate);

    /**
     * Trouve les taxes ordonnées par taux croissant
     */
    @Query("SELECT t FROM Tax t ORDER BY t.taxRate ASC")
    List<Tax> findAllOrderedByTaxRateAsc();

    /**
     * Trouve les taxes ordonnées par taux décroissant
     */
    @Query("SELECT t FROM Tax t ORDER BY t.taxRate DESC")
    List<Tax> findAllOrderedByTaxRateDesc();

    // ================== Recherche par Province ==================

    /**
     * Trouve les taxes par province
     */
    List<Tax> findByProvince(String province);

    /**
     * Trouve les taxes par province (ignorer la casse)
     */
    List<Tax> findByProvinceIgnoreCase(String province);

    // ================== Recherche par Panier ==================

    /**
     * Trouve les taxes appliquées à un panier spécifique
     */
    List<Tax> findByIdBasket(Long basketId);

    /**
     * Trouve les taxes non associées à un panier (configurations)
     */
    @Query("SELECT t FROM Tax t WHERE t.idBasket IS NULL")
    List<Tax> findConfigurations();

    /**
     * Trouve les taxes associées à des paniers (taxes appliquées)
     */
    @Query("SELECT t FROM Tax t WHERE t.idBasket IS NOT NULL")
    List<Tax> findAppliedTaxes();

    // ================== Requêtes Statistiques ==================

    /**
     * Compte le nombre de taxes par état
     */
    @Query("SELECT t.state, COUNT(t) FROM Tax t GROUP BY t.state")
    List<Object[]> countTaxesByState();

    /**
     * Trouve le taux de taxe moyen
     */
    @Query("SELECT AVG(t.taxRate) FROM Tax t WHERE t.active = 1")
    BigDecimal getAverageTaxRate();

    /**
     * Trouve le taux de taxe maximum
     */
    @Query("SELECT MAX(t.taxRate) FROM Tax t WHERE t.active = 1")
    BigDecimal getMaxTaxRate();

    /**
     * Trouve le taux de taxe minimum
     */
    @Query("SELECT MIN(t.taxRate) FROM Tax t WHERE t.active = 1")
    BigDecimal getMinTaxRate();

    // ================== Recherche Combinée ==================

    /**
     * Trouve une taxe active pour un état spécifique
     */
    @Query("SELECT t FROM Tax t WHERE t.state = :state AND t.active = 1 AND t.idBasket IS NULL")
    Optional<Tax> findActiveConfigurationByState(@Param("state") String state);

    /**
     * Trouve toutes les taxes actives ordonnées par état
     */
    @Query("SELECT t FROM Tax t WHERE t.active = 1 ORDER BY t.state ASC")
    List<Tax> findAllActiveOrderedByState();

    /**
     * Vérifie si un état a déjà une configuration de taxe
     */
    @Query("SELECT COUNT(t) > 0 FROM Tax t WHERE t.state = :state AND t.idBasket IS NULL")
    boolean existsConfigurationForState(@Param("state") String state);

    // ================== Recherche par Description ==================

    /**
     * Recherche partielle dans la description
     */
    @Query("SELECT t FROM Tax t WHERE t.description LIKE %:keyword%")
    List<Tax> findByDescriptionContaining(@Param("keyword") String keyword);

    /**
     * Trouve les taxes sans description
     */
    @Query("SELECT t FROM Tax t WHERE t.description IS NULL OR t.description = ''")
    List<Tax> findWithoutDescription();

    // ================== États Disponibles ==================

    /**
     * Obtient tous les états ayant des configurations de taxes
     */
    @Query("SELECT DISTINCT t.state FROM Tax t WHERE t.state IS NOT NULL AND t.idBasket IS NULL ORDER BY t.state")
    List<String> findAllStatesWithTaxConfigurations();

    /**
     * Obtient toutes les provinces ayant des configurations de taxes
     */
    @Query("SELECT DISTINCT t.province FROM Tax t WHERE t.province IS NOT NULL ORDER BY t.province")
    List<String> findAllProvincesWithTaxConfigurations();

    // ================== Montants de Taxes ==================

    /**
     * Trouve les taxes avec un montant spécifique
     */
    List<Tax> findByTaxAmount(BigDecimal amount);

    /**
     * Trouve les taxes avec un montant dans une plage
     */
    List<Tax> findByTaxAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Calcule le montant total des taxes pour un panier
     */
    @Query("SELECT SUM(t.taxAmount) FROM Tax t WHERE t.idBasket = :basketId")
    BigDecimal getTotalTaxAmountForBasket(@Param("basketId") Long basketId);
}