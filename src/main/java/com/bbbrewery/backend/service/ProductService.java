package com.bbbrewery.backend.service;

import com.bbbrewery.backend.dto.ProductDTO;
import com.bbbrewery.backend.model.Product;
import com.bbbrewery.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // =================== CONSULTATION DES PRODUITS ===================

    /**
     * Récupère tous les produits
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Récupère tous les produits actifs
     */
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    /**
     * Récupère tous les produits inactifs
     */
    @Transactional(readOnly = true)
    public List<Product> getInactiveProducts() {
        return productRepository.findByActiveFalse();
    }

    /**
     * Récupère un produit par son ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Recherche des produits par nom
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByProductNameContainingIgnoreCase(name);
    }

    /**
     * Récupère les produits par catégorie
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }

    /**
     * Récupère les produits par type
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByType(String type) {
        return productRepository.findByType(type);
    }

    /**
     * Récupère les produits par catégorie et type
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategoryAndType(String category, String type) {
        return productRepository.findByCategoryAndType(category, type);
    }

    /**
     * Récupère les produits en stock
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsInStock() {
        return productRepository.findProductsInStock();
    }

    /**
     * Récupère les produits en rupture de stock
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsOutOfStock() {
        return productRepository.findProductsOutOfStock();
    }

    /**
     * Récupère les produits avec stock faible (moins de 5)
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findByStockLessThan(5);
    }

    /**
     * Récupère les produits avec stock faible (seuil personnalisable)
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findProductsWithLowStock(threshold);
    }

    /**
     * Récupère les produits dans une fourchette de prix
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Récupère les produits en promotion
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsOnSale() {
        return productRepository.findProductsOnSale(LocalDateTime.now());
    }

    /**
     * Recherche des produits par description
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByDescription(String keyword) {
        return productRepository.findByDescriptionContaining(keyword);
    }

    /**
     * Recherche globale (nom ou description)
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    /**
     * Récupère les produits les plus vendus
     */
    @Transactional(readOnly = true)
    public List<Product> getTopSellingProducts() {
        return productRepository.findTopSellingProducts();
    }

    // =================== GESTION DES PRODUITS ===================

    /**
     * Crée un nouveau produit
     */
    public Product createProduct(ProductDTO productDTO) {
        Product product = convertDTOToEntity(productDTO);
        product.setActive(true);
        product.setCreatedDate(LocalDateTime.now());
        product.setUpdatedDate(LocalDateTime.now());
        return productRepository.save(product);
    }

    /**
     * Met à jour un produit existant
     */
    public Product updateProduct(Long id, ProductDTO productDTO) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            updateProductFromDTO(product, productDTO);
            product.setUpdatedDate(LocalDateTime.now());
            return productRepository.save(product);
        }
        throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
    }

    /**
     * Met à jour partiellement un produit
     */
    public Product updateProductPartial(Long id, ProductDTO productDTO) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            updateProductFromDTOPartial(product, productDTO);
            product.setUpdatedDate(LocalDateTime.now());
            return productRepository.save(product);
        }
        throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
    }

    /**
     * Supprime un produit
     */
    public void deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
        }
    }

    /**
     * Active/désactive un produit
     */
    public Product toggleProductStatus(Long id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setActive(!product.getActive());
            product.setUpdatedDate(LocalDateTime.now());
            return productRepository.save(product);
        }
        throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
    }

    /**
     * Active/désactive plusieurs produits
     */
    public int updateProductsStatus(List<Long> productIds, Boolean status) {
        return productRepository.updateProductsStatus(productIds, status, LocalDateTime.now());
    }

    // =================== GESTION DU STOCK ===================

    /**
     * Met à jour le stock d'un produit
     */
    public Product updateStock(Long id, int newStock) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setStock(newStock);
            product.setUpdatedDate(LocalDateTime.now());
            return productRepository.save(product);
        }
        throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
    }

    /**
     * Diminue le stock d'un produit
     */
    public boolean decreaseStock(Long productId, int quantity) {
        int updated = productRepository.decreaseStock(productId, quantity, LocalDateTime.now());
        return updated > 0;
    }

    /**
     * Augmente le stock d'un produit
     */
    public boolean increaseStock(Long productId, int quantity) {
        int updated = productRepository.increaseStock(productId, quantity, LocalDateTime.now());
        return updated > 0;
    }

    /**
     * Vérifie la disponibilité du stock
     */
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productId, int quantity) {
        Boolean result = productRepository.isStockAvailable(productId, quantity);
        return result != null && result;
    }

    // =================== PROCÉDURES STOCKÉES ===================

    /**
     * Met à jour la description d'un produit via procédure stockée
     */
    public void updateProductDescription(Long productId, String newDescription) {
        productRepository.updateProductDescription(productId, newDescription);
    }

    /**
     * Ajoute un nouveau produit via procédure stockée
     */
    public void addProductViaProcedure(String name, BigDecimal price, String description, int stock) {
        productRepository.addProductViaProcedure(name, price, description, stock);
    }

    // =================== VÉRIFICATIONS ===================

    /**
     * Vérifie si un produit existe
     */
    @Transactional(readOnly = true)
    public boolean productExists(Long id) {
        return productRepository.existsById(id);
    }

    /**
     * Vérifie si un produit existe et est actif
     */
    @Transactional(readOnly = true)
    public boolean productExistsAndActive(Long id) {
        Boolean result = productRepository.existsByIdAndActiveTrue(id);
        return result != null && result;
    }

    // =================== STATISTIQUES ===================

    /**
     * Obtient le nombre de produits actifs
     */
    @Transactional(readOnly = true)
    public Long getActiveProductsCount() {
        return productRepository.countActiveProducts();
    }

    /**
     * Obtient la quantité totale en stock
     */
    @Transactional(readOnly = true)
    public Long getTotalStockQuantity() {
        return productRepository.getTotalStockQuantity();
    }

    /**
     * Obtient le prix moyen des produits
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageProductPrice() {
        return productRepository.getAverageProductPrice();
    }

    /**
     * Obtient le nombre de produits par catégorie
     */
    @Transactional(readOnly = true)
    public List<Object[]> getProductCountByCategory() {
        return productRepository.countProductsByCategory();
    }

    /**
     * Obtient la valeur totale du stock
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalStockValue() {
        return productRepository.getTotalStockValue();
    }

    // =================== MÉTHODES UTILITAIRES PRIVÉES ===================

    /**
     * Convertit un ProductDTO en entité Product
     */
    private Product convertDTOToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setType(dto.getType());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setSalePrice(dto.getSalePrice());
        product.setSaleStartDate(dto.getSaleStartDate());
        product.setSaleEndDate(dto.getSaleEndDate());
        return product;
    }

    /**
     * Met à jour complètement un produit à partir d'un DTO
     */
    private void updateProductFromDTO(Product product, ProductDTO dto) {
        if (dto.getProductName() != null) {
            product.setProductName(dto.getProductName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }
        if (dto.getCategory() != null) {
            product.setCategory(dto.getCategory());
        }
        if (dto.getType() != null) {
            product.setType(dto.getType());
        }
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
        if (dto.getSalePrice() != null) {
            product.setSalePrice(dto.getSalePrice());
        }
        if (dto.getSaleStartDate() != null) {
            product.setSaleStartDate(dto.getSaleStartDate());
        }
        if (dto.getSaleEndDate() != null) {
            product.setSaleEndDate(dto.getSaleEndDate());
        }
    }

    /**
     * Met à jour partiellement un produit à partir d'un DTO (seuls les champs non null)
     */
    private void updateProductFromDTOPartial(Product product, ProductDTO dto) {
        // Cette méthode fait la même chose que updateProductFromDTO
        // Elle est là pour la clarté sémantique
        updateProductFromDTO(product, dto);
    }

    // =================== MÉTHODES DE VALIDATION ===================

    /**
     * Valide les données d'un produit avant création/modification
     */
    private void validateProductData(ProductDTO dto) {
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit est obligatoire");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit être supérieur à 0");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        }
        if (dto.getSalePrice() != null && dto.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix de vente doit être supérieur à 0");
        }
        if (dto.getSaleStartDate() != null && dto.getSaleEndDate() != null) {
            if (dto.getSaleStartDate().isAfter(dto.getSaleEndDate())) {
                throw new IllegalArgumentException("La date de début de promotion doit être antérieure à la date de fin");
            }
        }
    }

    /**
     * Crée un produit avec validation
     */
    public Product createProductWithValidation(ProductDTO productDTO) {
        validateProductData(productDTO);
        return createProduct(productDTO);
    }

    /**
     * Met à jour un produit avec validation
     */
    public Product updateProductWithValidation(Long id, ProductDTO productDTO) {
        validateProductData(productDTO);
        return updateProduct(id, productDTO);
    }
}