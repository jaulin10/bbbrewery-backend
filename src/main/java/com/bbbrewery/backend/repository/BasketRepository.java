package com.bbbrewery.backend.repository;

import com.bbbrewery.backend.model.BasketStatus;
import com.bbbrewery.backend.model.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {

    // ========== RECHERCHES DE BASE ==========

    /**
     * Recherche par shopper
     */
    @Query("SELECT b FROM Basket b WHERE b.shopper.id = :shopperId ORDER BY b.dateCreated DESC")
    List<Basket> findByShopperId(@Param("shopperId") Long shopperId);

    /**
     * Panier actif d'un shopper
     */
    @Query("SELECT b FROM Basket b WHERE b.shopper.id = :shopperId AND b.orderPlaced = :status")
    Optional<Basket> findActiveBasketByShopperId(@Param("shopperId") Long shopperId, @Param("status") BasketStatus status);

    /**
     * Recherche par statut
     */
    List<Basket> findByOrderPlaced(BasketStatus orderPlaced);

    /**
     * Recherche par plage de dates de création
     */
    List<Basket> findByDateCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Recherche par plage de dates de commande
     */
    List<Basket> findByDateOrderedBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ========== RECHERCHES AVANCÉES ==========

    /**
     * Paniers avec montant minimum
     */
    @Query("SELECT b FROM Basket b WHERE b.total >= :minAmount ORDER BY b.total DESC")
    List<Basket> findBasketsByMinimumTotal(@Param("minAmount") BigDecimal minAmount);

    /**
     * Paniers récents
     */
    @Query("SELECT b FROM Basket b WHERE b.dateCreated >= :since ORDER BY b.dateCreated DESC")
    List<Basket> findRecentBaskets(@Param("since") LocalDateTime since);

    /**
     * Paniers abandonnés (actifs avec articles mais anciens)
     */
    @Query("SELECT b FROM Basket b WHERE b.orderPlaced = :activeStatus " +
            "AND b.dateCreated < :cutoffDate AND SIZE(b.basketItems) > 0 " +
            "ORDER BY b.dateCreated ASC")
    List<Basket> findAbandonedBaskets(@Param("cutoffDate") LocalDateTime cutoffDate,
                                      @Param("activeStatus") BasketStatus activeStatus);

    // Version avec paramètre par défaut
    default List<Basket> findAbandonedBaskets(LocalDateTime cutoffDate) {
        return findAbandonedBaskets(cutoffDate, BasketStatus.ACTIVE);
    }

    // ========== RECHERCHES OPTIMISÉES AVEC FETCH JOIN ==========

    /**
     * Recherche avec join fetch pour optimisation (charge les articles)
     */
    @Query("SELECT DISTINCT b FROM Basket b " +
            "LEFT JOIN FETCH b.basketItems bi " +
            "LEFT JOIN FETCH bi.product " +
            "WHERE b.id = :basketId")
    Optional<Basket> findByIdWithItems(@Param("basketId") Long basketId);

    /**
     * Panier actif avec articles d'un shopper (optimisé)
     */
    @Query("SELECT DISTINCT b FROM Basket b " +
            "LEFT JOIN FETCH b.basketItems bi " +
            "LEFT JOIN FETCH bi.product " +
            "LEFT JOIN FETCH b.shopper " +
            "WHERE b.shopper.id = :shopperId AND b.orderPlaced = :status")
    Optional<Basket> findActiveBasketWithItemsByShopperId(@Param("shopperId") Long shopperId,
                                                          @Param("status") BasketStatus status);

    /**
     * Recherche avec tous les détails (shopper, articles, produits)
     */
    @Query("SELECT DISTINCT b FROM Basket b " +
            "LEFT JOIN FETCH b.basketItems bi " +
            "LEFT JOIN FETCH bi.product " +
            "LEFT JOIN FETCH b.shopper " +
           // "LEFT JOIN FETCH b.taxes " +
            "WHERE b.id = :basketId")
    Optional<Basket> findByIdWithAllDetails(@Param("basketId") Long basketId);

    // ========== PROCÉDURES STOCKÉES ET FONCTIONS ==========

    /**
     * Ajout d'un article au panier via procédure stockée
     */
    @Modifying
    @Transactional
    @Query(value = "BEGIN basket_add_sp(:basketId, :productId, :price, :quantity, :option1, :option2); END;",
            nativeQuery = true)
    void addItemToBasketViaProcedure(@Param("basketId") Long basketId,
                                     @Param("productId") Long productId,
                                     @Param("price") BigDecimal price,
                                     @Param("quantity") Integer quantity,
                                     @Param("option1") String option1,
                                     @Param("option2") String option2);

    /**
     * Calcul du total d'achat d'un shopper via fonction
     */
    @Query(value = "SELECT NVL(tot_purch_sf(:shopperId), 0) FROM DUAL", nativeQuery = true)
    BigDecimal getTotalPurchasesByShopperId(@Param("shopperId") Long shopperId);

    /**
     * Vérification de vente via fonction
     */
    @Query(value = "SELECT ck_sale_sf(:productId) FROM DUAL", nativeQuery = true)
    Integer checkProductOnSale(@Param("productId") Long productId);

    // ========== MISES À JOUR BULK ==========

    /**
     * Mise à jour des totaux du panier
     */
    @Modifying
    @Transactional
    @Query("UPDATE Basket b SET b.subtotal = :subtotal, b.tax = :tax, b.shipping = :shipping, b.total = :total " +
            "WHERE b.id = :basketId")
    int updateBasketTotals(@Param("basketId") Long basketId,
                           @Param("subtotal") BigDecimal subtotal,
                           @Param("tax") BigDecimal tax,
                           @Param("shipping") BigDecimal shipping,
                           @Param("total") BigDecimal total);

    /**
     * Mise à jour du statut de commande
     */
    @Modifying
    @Transactional
    @Query("UPDATE Basket b SET b.orderPlaced = :status, b.dateOrdered = :orderDate " +
            "WHERE b.id = :basketId")
    int updateOrderStatus(@Param("basketId") Long basketId,
                          @Param("status") BasketStatus status,
                          @Param("orderDate") LocalDateTime orderDate);

    /**
     * Mise à jour du statut pour plusieurs paniers
     */
    @Modifying
    @Transactional
    @Query("UPDATE Basket b SET b.orderPlaced = :newStatus " +
            "WHERE b.orderPlaced = :currentStatus AND b.dateCreated < :beforeDate")
    int updateBasketStatusBulk(@Param("currentStatus") BasketStatus currentStatus,
                               @Param("newStatus") BasketStatus newStatus,
                               @Param("beforeDate") LocalDateTime beforeDate);

    // ========== STATISTIQUES ==========

    /**
     * Compte les paniers par statut
     */
    @Query("SELECT COUNT(b) FROM Basket b WHERE b.orderPlaced = :status")
    Long countBasketsByStatus(@Param("status") BasketStatus status);

    /**
     * Chiffre d'affaires total par statut
     */
    @Query("SELECT COALESCE(SUM(b.total), 0) FROM Basket b WHERE b.orderPlaced = :status")
    BigDecimal getTotalRevenueByStatus(@Param("status") BasketStatus status);

    /**
     * Valeur moyenne des paniers par statut
     */
    @Query("SELECT COALESCE(AVG(b.total), 0) FROM Basket b WHERE b.orderPlaced = :status")
    BigDecimal getAverageBasketValueByStatus(@Param("status") BasketStatus status);

    /**
     * Nombre de paniers créés dans une période
     */
    @Query("SELECT COUNT(b) FROM Basket b WHERE b.dateCreated BETWEEN :startDate AND :endDate")
    Long countBasketsInPeriod(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);

    /**
     * Ventes totales dans une période (paniers non actifs uniquement)
     */
    @Query("SELECT COALESCE(SUM(b.total), 0) FROM Basket b " +
            "WHERE b.dateOrdered BETWEEN :startDate AND :endDate " +
            "AND b.orderPlaced <> :activeStatus")
    BigDecimal getTotalSalesInPeriod(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("activeStatus") BasketStatus activeStatus);

    // Version avec paramètre par défaut
    default BigDecimal getTotalSalesInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return getTotalSalesInPeriod(startDate, endDate, BasketStatus.ACTIVE);
    }

    /**
     * Meilleurs clients par dépenses totales
     */
    @Query("SELECT b.shopper.id, b.shopper.firstName, b.shopper.lastName, " +
            "COUNT(b), COALESCE(SUM(b.total), 0) " +
            "FROM Basket b WHERE b.orderPlaced <> :activeStatus " +
            "GROUP BY b.shopper.id, b.shopper.firstName, b.shopper.lastName " +
            "ORDER BY SUM(b.total) DESC")
    List<Object[]> getTopCustomersBySpending(@Param("activeStatus") BasketStatus activeStatus);

    // Version avec paramètre par défaut
    default List<Object[]> getTopCustomersBySpending() {
        return getTopCustomersBySpending(BasketStatus.ACTIVE);
    }

    /**
     * Statistiques des paniers par mois
     */
    @Query(value = """
        SELECT EXTRACT(YEAR FROM b.date_created) as year,
               EXTRACT(MONTH FROM b.date_created) as month,
               COUNT(*) as basket_count,
               COALESCE(SUM(b.total), 0) as total_revenue,
               COALESCE(AVG(b.total), 0) as avg_basket_value
        FROM BB_BASKET b
        WHERE b.date_created BETWEEN :startDate AND :endDate
        GROUP BY EXTRACT(YEAR FROM b.date_created), EXTRACT(MONTH FROM b.date_created)
        ORDER BY year DESC, month DESC
        """, nativeQuery = true)
    List<Object[]> getMonthlyBasketStatistics(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Taux d'abandon des paniers
     */
    @Query(value = """
        SELECT 
            (SELECT COUNT(*) FROM BB_BASKET WHERE order_placed = 0 AND dt_created < :cutoffDate) as abandoned_count,
            (SELECT COUNT(*) FROM BB_BASKET WHERE dt_created < :cutoffDate) as total_count,
            CASE 
                WHEN (SELECT COUNT(*) FROM BB_BASKET WHERE dt_created < :cutoffDate) > 0
                THEN (SELECT COUNT(*) FROM BB_BASKET WHERE order_placed = 0 AND dt_created < :cutoffDate) * 100.0 / 
                     (SELECT COUNT(*) FROM BB_BASKET WHERE dt_created < :cutoffDate)
                ELSE 0 
            END as abandonment_rate
        FROM DUAL
        """, nativeQuery = true)
    Object[] getBasketAbandonmentRate(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== REQUÊTES DE VALIDATION ==========

    /**
     * Vérifie si un panier existe et est actif
     */
    @Query("SELECT COUNT(b) > 0 FROM Basket b WHERE b.id = :basketId AND b.orderPlaced = :activeStatus")
    boolean existsActiveBasket(@Param("basketId") Long basketId, @Param("activeStatus") BasketStatus activeStatus);

    // Version avec paramètre par défaut
    default boolean existsActiveBasket(Long basketId) {
        return existsActiveBasket(basketId, BasketStatus.ACTIVE);
    }

    /**
     * Vérifie si un shopper a un panier actif
     */
    @Query("SELECT COUNT(b) > 0 FROM Basket b WHERE b.shopper.id = :shopperId AND b.orderPlaced = :activeStatus")
    boolean hasActiveBasket(@Param("shopperId") Long shopperId, @Param("activeStatus") BasketStatus activeStatus);

    // Version avec paramètre par défaut
    default boolean hasActiveBasket(Long shopperId) {
        return hasActiveBasket(shopperId, BasketStatus.ACTIVE);
    }
}