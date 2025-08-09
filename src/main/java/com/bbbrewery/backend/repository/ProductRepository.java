package com.bbbrewery.backend.repository;

import com.bbbrewery.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // =================== RECHERCHES DE BASE ===================

    /**
     * Recherche par nom de produit (insensible à la casse)
     */
    List<Product> findByProductNameContainingIgnoreCase(String productName);

    /**
     * Recherche par catégorie (insensible à la casse)
     */
    List<Product> findByCategoryIgnoreCase(String category);

    /**
     * Recherche par statut actif
     */
    List<Product> findByActiveTrue();

    List<Product> findByActiveFalse();

    /**
     * Recherche par type
     */
    List<Product> findByType(String type);

    /**
     * Recherche par plage de prix
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Produits avec stock faible (seuil personnalisable)
     */
    List<Product> findByStockLessThan(Integer threshold);

    /**
     * Produits avec stock exact
     */
    List<Product> findByStock(Integer stock);

    // =================== REQUÊTES PERSONNALISÉES ===================

    /**
     * Recherche des produits en stock
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.active = true")
    List<Product> findProductsInStock();

    /**
     * Recherche des produits en rupture de stock
     */
    @Query("SELECT p FROM Product p WHERE p.stock = 0 AND p.active = true")
    List<Product> findProductsOutOfStock();

    /**
     * Recherche des produits en promotion actuellement
     */
    @Query("SELECT p FROM Product p WHERE p.salePrice IS NOT NULL " +
            "AND p.saleStartDate <= :currentDate " +
            "AND p.saleEndDate >= :currentDate " +
            "AND p.active = true")
    List<Product> findProductsOnSale(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Recherche des produits par description
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.active = true")
    List<Product> findByDescriptionContaining(@Param("keyword") String keyword);

    /**
     * Recherche combinée (nom ou description)
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.active = true")
    List<Product> searchProducts(@Param("keyword") String keyword);

    /**
     * Produits avec stock faible et actifs
     */
    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.active = true")
    List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);

    /**
     * Top produits par ventes
     */
    @Query(value = "SELECT p.* FROM BB_PRODUCT p " +
            "JOIN BB_BASKETITEM bi ON p.IDPRODUCT = bi.IDPRODUCT " +
            "JOIN BB_BASKET b ON bi.IDBASKET = b.IDBASKET " +
            "WHERE b.ORDERPLACED = 1 AND p.ACTIVE = 1 " +
            "GROUP BY p.IDPRODUCT, p.PRODUCTNAME, p.DESCRIPTION, p.PRICE, p.STOCK, " +
            "         p.ACTIVE, p.SALEPRICE, p.SALESTARTDATE, p.SALEENDDATE, " +
            "         p.CATEGORY, p.TYPE, p.IMAGEURL, p.CREATED_DATE, p.UPDATED_DATE " +
            "ORDER BY SUM(bi.QUANTITY) DESC",
            nativeQuery = true)
    List<Product> findTopSellingProducts();

    /**
     * Produits par catégorie et type
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
            "(:type IS NULL OR p.type = :type) AND " +
            "p.active = true")
    List<Product> findByCategoryAndType(@Param("category") String category, @Param("type") String type);

    // =================== VÉRIFICATIONS ===================

    /**
     * Vérification de disponibilité du stock
     */
    @Query("SELECT CASE WHEN p.stock >= :quantity THEN true ELSE false END " +
            "FROM Product p WHERE p.id = :productId")
    Boolean isStockAvailable(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Vérifier si un produit existe et est actif
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Product p WHERE p.id = :productId AND p.active = true")
    Boolean existsByIdAndActiveTrue(@Param("productId") Long productId);

    // =================== STATISTIQUES ===================

    /**
     * Nombre de produits actifs
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    Long countActiveProducts();

    /**
     * Stock total des produits actifs
     */
    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM Product p WHERE p.active = true")
    Long getTotalStockQuantity();

    /**
     * Prix moyen des produits actifs
     */
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.active = true")
    BigDecimal getAverageProductPrice();

    /**
     * Nombre de produits par catégorie
     */
    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.active = true GROUP BY p.category")
    List<Object[]> countProductsByCategory();

    /**
     * Valeur totale du stock
     */
    @Query("SELECT COALESCE(SUM(p.price * p.stock), 0) FROM Product p WHERE p.active = true")
    BigDecimal getTotalStockValue();

    // =================== MODIFICATIONS ===================

    /**
     * Diminuer le stock
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity, p.updatedDate = :updateTime " +
            "WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId,
                      @Param("quantity") Integer quantity,
                      @Param("updateTime") LocalDateTime updateTime);

    /**
     * Augmenter le stock
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity, p.updatedDate = :updateTime " +
            "WHERE p.id = :productId")
    int increaseStock(@Param("productId") Long productId,
                      @Param("quantity") Integer quantity,
                      @Param("updateTime") LocalDateTime updateTime);

    /**
     * Activer/Désactiver des produits en lot
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.active = :status, p.updatedDate = :updateTime WHERE p.id IN :productIds")
    int updateProductsStatus(@Param("productIds") List<Long> productIds,
                             @Param("status") Boolean status,
                             @Param("updateTime") LocalDateTime updateTime);

    // =================== PROCÉDURES STOCKÉES ===================

    /**
     * Mise à jour de la description via procédure stockée
     */
    @Modifying
    @Transactional
    @Query(value = "BEGIN prod_description_update_sp(:productId, :newDescription); END;", nativeQuery = true)
    void updateProductDescription(@Param("productId") Long productId, @Param("newDescription") String newDescription);

    /**
     * Ajout d'un produit via procédure stockée
     */
    @Modifying
    @Transactional
    @Query(value = "BEGIN prod_add_sp(:productName, :price, :description, :stock); END;", nativeQuery = true)
    void addProductViaProcedure(@Param("productName") String productName,
                                @Param("price") BigDecimal price,
                                @Param("description") String description,
                                @Param("stock") Integer stock);
}