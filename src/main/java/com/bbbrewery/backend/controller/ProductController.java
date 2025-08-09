package com.bbbrewery.backend.controller;

import com.bbbrewery.backend.dto.ProductDTO;
import jakarta.validation.Valid;
import com.bbbrewery.backend.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bbbrewery.backend.service.ProductService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"}, allowCredentials = "false")
public class ProductController {

    @Autowired
    private ProductService productService;

    // =================== CONSULTATION DES PRODUITS ===================

    /**
     * Récupère tous les produits
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère tous les produits actifs
     * GET /api/products/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        try {
            List<Product> products = productService.getActiveProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère tous les produits inactifs
     * GET /api/products/inactive
     */
    @GetMapping("/inactive")
    public ResponseEntity<List<Product>> getInactiveProducts() {
        try {
            List<Product> products = productService.getInactiveProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un produit par son ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productService.getProductById(id);
            return product.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des produits par nom
     * GET /api/products/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(@RequestParam String name) {
        try {
            List<Product> products = productService.searchProductsByName(name);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche globale des produits (nom ou description)
     * GET /api/products/search/global?keyword={keyword}
     */
    @GetMapping("/search/global")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        try {
            List<Product> products = productService.searchProducts(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des produits par description
     * GET /api/products/search/description?keyword={keyword}
     */
    @GetMapping("/search/description")
    public ResponseEntity<List<Product>> searchProductsByDescription(@RequestParam String keyword) {
        try {
            List<Product> products = productService.searchProductsByDescription(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits par catégorie
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits par type
     * GET /api/products/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Product>> getProductsByType(@PathVariable String type) {
        try {
            List<Product> products = productService.getProductsByType(type);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits par catégorie et type
     * GET /api/products/filter?category={category}&type={type}
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> getProductsByCategoryAndType(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type) {
        try {
            List<Product> products = productService.getProductsByCategoryAndType(category, type);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits en stock
     * GET /api/products/in-stock
     */
    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> getProductsInStock() {
        try {
            List<Product> products = productService.getProductsInStock();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits en rupture de stock
     * GET /api/products/out-of-stock
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getProductsOutOfStock() {
        try {
            List<Product> products = productService.getProductsOutOfStock();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits avec stock faible (par défaut < 5)
     * GET /api/products/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        try {
            List<Product> products = productService.getLowStockProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits avec stock faible (seuil personnalisable)
     * GET /api/products/low-stock/{threshold}
     */
    @GetMapping("/low-stock/{threshold}")
    public ResponseEntity<List<Product>> getLowStockProducts(@PathVariable int threshold) {
        try {
            List<Product> products = productService.getLowStockProducts(threshold);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits dans une fourchette de prix
     * GET /api/products/price-range?min={min}&max={max}
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        try {
            List<Product> products = productService.getProductsByPriceRange(min, max);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits en promotion
     * GET /api/products/on-sale
     */
    @GetMapping("/on-sale")
    public ResponseEntity<List<Product>> getProductsOnSale() {
        try {
            List<Product> products = productService.getProductsOnSale();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits les plus vendus
     * GET /api/products/top-selling
     */
    @GetMapping("/top-selling")
    public ResponseEntity<List<Product>> getTopSellingProducts() {
        try {
            List<Product> products = productService.getTopSellingProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== GESTION DES PRODUITS ===================

    /**
     * Crée un nouveau produit
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            Product createdProduct = productService.createProductWithValidation(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour un produit existant
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @Valid @RequestBody ProductDTO productDTO) {
        try {
            Product updatedProduct = productService.updateProductWithValidation(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * Met à jour partiellement un produit
     * PATCH /api/products/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProductPartial(@PathVariable Long id,
                                                        @RequestBody ProductDTO productDTO) {
        try {
            Product updatedProduct = productService.updateProductPartial(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un produit
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Active/désactive un produit
     * PATCH /api/products/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Product> toggleProductStatus(@PathVariable Long id) {
        try {
            Product updatedProduct = productService.toggleProductStatus(id);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Active/désactive plusieurs produits
     * PATCH /api/products/batch/status
     */
    @PatchMapping("/batch/status")
    public ResponseEntity<Map<String, Object>> updateProductsStatus(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> productIds = (List<Long>) request.get("productIds");
            Boolean status = (Boolean) request.get("status");

            int updatedCount = productService.updateProductsStatus(productIds, status);

            Map<String, Object> response = new HashMap<>();
            response.put("updatedCount", updatedCount);
            response.put("status", status);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== GESTION DU STOCK ===================

    /**
     * Met à jour le stock d'un produit
     * PATCH /api/products/{id}/stock
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id,
                                               @RequestParam int stock) {
        try {
            Product updatedProduct = productService.updateStock(id, stock);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Diminue le stock d'un produit
     * PATCH /api/products/{id}/stock/decrease
     */
    @PatchMapping("/{id}/stock/decrease")
    public ResponseEntity<Map<String, Object>> decreaseStock(@PathVariable Long id,
                                                             @RequestParam int quantity) {
        try {
            boolean success = productService.decreaseStock(id, quantity);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Stock diminué avec succès" : "Stock insuffisant");

            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Augmente le stock d'un produit
     * PATCH /api/products/{id}/stock/increase
     */
    @PatchMapping("/{id}/stock/increase")
    public ResponseEntity<Map<String, Object>> increaseStock(@PathVariable Long id,
                                                             @RequestParam int quantity) {
        try {
            boolean success = productService.increaseStock(id, quantity);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Stock augmenté avec succès" : "Erreur lors de l'augmentation du stock");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vérifie la disponibilité du stock
     * GET /api/products/{id}/stock/check?quantity={quantity}
     */
    @GetMapping("/{id}/stock/check")
    public ResponseEntity<Map<String, Object>> checkStockAvailability(@PathVariable Long id,
                                                                      @RequestParam int quantity) {
        try {
            boolean available = productService.isStockAvailable(id, quantity);
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("productId", id);
            response.put("requestedQuantity", quantity);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== PROCÉDURES STOCKÉES ===================

    /**
     * Met à jour la description d'un produit via procédure stockée
     * PATCH /api/products/{id}/description
     */
    @PatchMapping("/{id}/description")
    public ResponseEntity<Void> updateProductDescription(@PathVariable Long id,
                                                         @RequestBody Map<String, String> request) {
        try {
            String description = request.get("description");
            productService.updateProductDescription(id, description);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ajoute un produit via procédure stockée
     * POST /api/products/procedure
     */
    @PostMapping("/procedure")
    public ResponseEntity<Void> addProductViaProcedure(@RequestBody ProductDTO productDTO) {
        try {
            productService.addProductViaProcedure(
                    productDTO.getProductName(),
                    productDTO.getPrice(),
                    productDTO.getDescription(),
                    productDTO.getStock()
            );
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // =================== VÉRIFICATIONS ===================

    /**
     * Vérifie si un produit existe
     * HEAD /api/products/{id}
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkProductExists(@PathVariable Long id) {
        try {
            boolean exists = productService.productExists(id);
            return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vérifie si un produit existe et est actif
     * GET /api/products/{id}/active-check
     */
    @GetMapping("/{id}/active-check")
    public ResponseEntity<Map<String, Object>> checkProductExistsAndActive(@PathVariable Long id) {
        try {
            boolean existsAndActive = productService.productExistsAndActive(id);
            Map<String, Object> response = new HashMap<>();
            response.put("existsAndActive", existsAndActive);
            response.put("productId", id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== STATISTIQUES ===================

    /**
     * Obtient les statistiques des produits
     * GET /api/products/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getProductStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("activeProductsCount", productService.getActiveProductsCount());
            statistics.put("totalStockQuantity", productService.getTotalStockQuantity());
            statistics.put("averagePrice", productService.getAverageProductPrice());
            statistics.put("totalStockValue", productService.getTotalStockValue());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtient le nombre de produits par catégorie
     * GET /api/products/statistics/by-category
     */
    @GetMapping("/statistics/by-category")
    public ResponseEntity<List<Object[]>> getProductCountByCategory() {
        try {
            List<Object[]> statistics = productService.getProductCountByCategory();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}