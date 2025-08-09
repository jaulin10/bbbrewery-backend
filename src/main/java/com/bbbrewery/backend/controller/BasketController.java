package com.bbbrewery.backend.controller;

import com.bbbrewery.backend.model.Basket;
import com.bbbrewery.backend.model.BasketStatus;
import com.bbbrewery.backend.service.BasketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/baskets")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"}, allowCredentials = "false")
public class BasketController {

    @Autowired
    private BasketService basketService;

    // ========== RÉCUPÉRATION DES PANIERS ==========
    /**
     * Test endpoint - données simulées
     * GET /api/baskets/test
     */
    @GetMapping("/test")
    public ResponseEntity<List<Map<String, Object>>> testEndpoint() {
        List<Map<String, Object>> mockData = new ArrayList<>();

        Map<String, Object> basket1 = new HashMap<>();
        basket1.put("id", 1L);
        basket1.put("total", 25.50);
        basket1.put("shopperId", 1L);
        mockData.add(basket1);

        Map<String, Object> basket2 = new HashMap<>();
        basket2.put("id", 2L);
        basket2.put("total", 45.00);
        basket2.put("shopperId", 2L);
        mockData.add(basket2);

        return ResponseEntity.ok(mockData);
    }

    /**
     * Récupère tous les paniers
     * GET /api/baskets
     */
    /**
    @GetMapping
    public ResponseEntity<List<Basket>> getAllBaskets() {
        try {
            List<Basket> baskets = basketService.getAllBaskets();
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    } */

    /**
     * Ajoute un article au panier (simulation)
     * POST /api/basket-items
     */
    @PostMapping("/basket-items")
    public ResponseEntity<Map<String, Object>> addItemToBasket(@RequestBody Map<String, Object> itemData) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Article ajouté au panier");
            response.put("basketId", itemData.get("basketId"));
            response.put("productId", itemData.get("productId"));
            response.put("quantity", itemData.get("quantity"));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBaskets() {
        try {
            // Données simulées en attendant la configuration complète
            List<Map<String, Object>> mockBaskets = new ArrayList<>();

            Map<String, Object> basket1 = new HashMap<>();
            basket1.put("id", 1L);
            basket1.put("total", 25.50);
            basket1.put("shopperId", 1L);
            basket1.put("dtcreated", LocalDateTime.now().toString());
            basket1.put("orderplaced", false);
            mockBaskets.add(basket1);

            Map<String, Object> basket2 = new HashMap<>();
            basket2.put("id", 2L);
            basket2.put("total", 45.00);
            basket2.put("shopperId", 2L);
            basket2.put("dtcreated", LocalDateTime.now().toString());
            basket2.put("orderplaced", false);
            mockBaskets.add(basket2);

            Map<String, Object> basket3 = new HashMap<>();
            basket3.put("id", 3L);
            basket3.put("total", 0.0);
            basket3.put("shopperId", 3L);
            basket3.put("dtcreated", LocalDateTime.now().toString());
            basket3.put("orderplaced", false);
            mockBaskets.add(basket3);

            return ResponseEntity.ok(mockBaskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un panier par son ID
     * GET /api/baskets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Basket> getBasketById(@PathVariable Long id) {
        try {
            Optional<Basket> basket = basketService.getBasketById(id);
            return basket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un panier avec ses articles par ID (optimisé)
     * GET /api/baskets/{id}/with-items
     */
    @GetMapping("/{id}/with-items")
    public ResponseEntity<Basket> getBasketByIdWithItems(@PathVariable Long id) {
        try {
            Optional<Basket> basket = basketService.getBasketByIdWithItems(id);
            return basket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les paniers d'un client
     * GET /api/baskets/shopper/{shopperId}
     */
    @GetMapping("/shopper/{shopperId}")
    public ResponseEntity<List<Basket>> getBasketsByShopperId(@PathVariable Long shopperId) {
        try {
            List<Basket> baskets = basketService.getBasketsByShopperId(shopperId);
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère le panier actif d'un client
     * GET /api/baskets/shopper/{shopperId}/active
     */
    @GetMapping("/shopper/{shopperId}/active")
    public ResponseEntity<Basket> getActiveBasketByShopperId(@PathVariable Long shopperId) {
        try {
            Optional<Basket> basket = basketService.getActiveBasketByShopperId(shopperId);
            return basket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère le panier actif avec articles d'un client (optimisé)
     * GET /api/baskets/shopper/{shopperId}/active/with-items
     */
    @GetMapping("/shopper/{shopperId}/active/with-items")
    public ResponseEntity<Basket> getActiveBasketWithItemsByShopperId(@PathVariable Long shopperId) {
        try {
            Optional<Basket> basket = basketService.getActiveBasketWithItemsByShopperId(shopperId);
            return basket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les paniers par statut
     * GET /api/baskets/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Basket>> getBasketsByStatus(@PathVariable BasketStatus status) {
        try {
            List<Basket> baskets = basketService.getBasketsByStatus(status);
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les paniers par période de création
     * GET /api/baskets/created-between
     */
    @GetMapping("/created-between")
    public ResponseEntity<List<Basket>> getBasketsByDateCreated(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Basket> baskets = basketService.getBasketsByDateCreated(startDate, endDate);
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les paniers par période de commande
     * GET /api/baskets/ordered-between
     */
    @GetMapping("/ordered-between")
    public ResponseEntity<List<Basket>> getBasketsByDateOrdered(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Basket> baskets = basketService.getBasketsByDateOrdered(startDate, endDate);
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les paniers avec montant minimum
     * GET /api/baskets/minimum-total/{minAmount}
     */
    @GetMapping("/minimum-total/{minAmount}")
    public ResponseEntity<List<Basket>> getBasketsByMinimumTotal(@PathVariable BigDecimal minAmount) {
        try {
            List<Basket> baskets = basketService.getBasketsByMinimumTotal(minAmount);
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les paniers récents
     * GET /api/baskets/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Basket>> getRecentBaskets(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<Basket> baskets = basketService.getRecentBaskets(since);
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les paniers abandonnés
     * GET /api/baskets/abandoned
     */
    @GetMapping("/abandoned")
    public ResponseEntity<List<Basket>> getAbandonedBaskets(
            @RequestParam(defaultValue = "7") int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            List<Basket> baskets = basketService.getAbandonedBaskets(cutoffDate);
            return ResponseEntity.ok(baskets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== CRÉATION ET MODIFICATION ==========

    /**
     * Crée un nouveau panier
     * POST /api/baskets
     */
    @PostMapping
    public ResponseEntity<Basket> createBasket(@RequestParam Long shopperId) {
        try {
            Basket createdBasket = basketService.createBasket(shopperId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ajoute un article au panier
     * POST /api/baskets/{basketId}/items
     */
    @PostMapping("/{basketId}/items")
    public ResponseEntity<Basket> addItemToBasket(@PathVariable Long basketId,
                                                  @RequestParam Long productId,
                                                  @RequestParam int quantity) {
        try {
            Basket updatedBasket = basketService.addItemToBasket(basketId, productId, quantity);
            return ResponseEntity.ok(updatedBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ajoute un article via procédure stockée
     * POST /api/baskets/{basketId}/items/procedure
     */
    @PostMapping("/{basketId}/items/procedure")
    public ResponseEntity<Void> addItemViaProcedure(@PathVariable Long basketId,
                                                    @RequestParam Long productId,
                                                    @RequestParam int quantity) {
        try {
            basketService.addItemViaProcedure(basketId, productId, quantity);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour la quantité d'un article
     * PUT /api/baskets/{basketId}/items/{productId}
     */
    @PutMapping("/{basketId}/items/{productId}")
    public ResponseEntity<Basket> updateItemQuantity(@PathVariable Long basketId,
                                                     @PathVariable Long productId,
                                                     @RequestParam int quantity) {
        try {
            Basket updatedBasket = basketService.updateItemQuantity(basketId, productId, quantity);
            return ResponseEntity.ok(updatedBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un article du panier
     * DELETE /api/baskets/{basketId}/items/{productId}
     */
    @DeleteMapping("/{basketId}/items/{productId}")
    public ResponseEntity<Basket> removeItemFromBasket(@PathVariable Long basketId,
                                                       @PathVariable Long productId) {
        try {
            Basket updatedBasket = basketService.removeItemFromBasket(basketId, productId);
            return ResponseEntity.ok(updatedBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vide le panier
     * DELETE /api/baskets/{basketId}/items
     */
    @DeleteMapping("/{basketId}/items")
    public ResponseEntity<Basket> clearBasket(@PathVariable Long basketId) {
        try {
            Basket clearedBasket = basketService.clearBasket(basketId);
            return ResponseEntity.ok(clearedBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un panier
     * DELETE /api/baskets/{basketId}
     */
    @DeleteMapping("/{basketId}")
    public ResponseEntity<Void> deleteBasket(@PathVariable Long basketId) {
        try {
            basketService.deleteBasket(basketId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== FINALISATION ET STATUT ==========

    /**
     * Finalise une commande (checkout)
     * POST /api/baskets/{basketId}/checkout
     */
    @PostMapping("/{basketId}/checkout")
    public ResponseEntity<Basket> checkoutBasket(@PathVariable Long basketId) {
        try {
            Basket checkedOutBasket = basketService.checkoutBasket(basketId);
            return ResponseEntity.ok(checkedOutBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour le statut du panier
     * PATCH /api/baskets/{basketId}/status
     */
    @PatchMapping("/{basketId}/status")
    public ResponseEntity<Basket> updateBasketStatus(@PathVariable Long basketId,
                                                     @RequestParam BasketStatus status) {
        try {
            Basket updatedBasket = basketService.updateBasketStatus(basketId, status);
            return ResponseEntity.ok(updatedBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== TAXES ET FRAIS ==========

    /**
     * Met à jour les taxes du panier
     * PATCH /api/baskets/{basketId}/tax
     */
    @PatchMapping("/{basketId}/tax")
    public ResponseEntity<Basket> updateBasketTax(@PathVariable Long basketId,
                                                  @RequestParam BigDecimal tax) {
        try {
            Basket updatedBasket = basketService.updateBasketTax(basketId, tax);
            return ResponseEntity.ok(updatedBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour les frais de port du panier
     * PATCH /api/baskets/{basketId}/shipping
     */
    @PatchMapping("/{basketId}/shipping")
    public ResponseEntity<Basket> updateBasketShipping(@PathVariable Long basketId,
                                                       @RequestParam BigDecimal shipping) {
        try {
            Basket updatedBasket = basketService.updateBasketShipping(basketId, shipping);
            return ResponseEntity.ok(updatedBasket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== INFORMATIONS ET STATISTIQUES ==========

    /**
     * Compte le nombre d'articles dans le panier
     * GET /api/baskets/{basketId}/item-count
     */
    @GetMapping("/{basketId}/item-count")
    public ResponseEntity<Integer> getItemCount(@PathVariable Long basketId) {
        try {
            int itemCount = basketService.getItemCount(basketId);
            return ResponseEntity.ok(itemCount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calcule le nombre total d'articles (somme des quantités)
     * GET /api/baskets/{basketId}/total-quantity
     */
    @GetMapping("/{basketId}/total-quantity")
    public ResponseEntity<Integer> getTotalItemQuantity(@PathVariable Long basketId) {
        try {
            int totalQuantity = basketService.getTotalItemQuantity(basketId);
            return ResponseEntity.ok(totalQuantity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère le total des achats d'un client via fonction stockée
     * GET /api/baskets/shopper/{shopperId}/total-purchases
     */
    @GetMapping("/shopper/{shopperId}/total-purchases")
    public ResponseEntity<BigDecimal> getTotalPurchasesByShopperId(@PathVariable Long shopperId) {
        try {
            BigDecimal totalPurchases = basketService.getTotalPurchasesByShopperId(shopperId);
            return ResponseEntity.ok(totalPurchases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vérifie si un produit est en promotion via fonction stockée
     * GET /api/baskets/product/{productId}/on-sale
     */
    @GetMapping("/product/{productId}/on-sale")
    public ResponseEntity<Boolean> checkProductOnSale(@PathVariable Long productId) {
        try {
            boolean onSale = basketService.checkProductOnSale(productId);
            return ResponseEntity.ok(onSale);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== STATISTIQUES ==========

    /**
     * Compte les paniers par statut
     * GET /api/baskets/statistics/count-by-status/{status}
     */
    @GetMapping("/statistics/count-by-status/{status}")
    public ResponseEntity<Long> countBasketsByStatus(@PathVariable BasketStatus status) {
        try {
            Long count = basketService.countBasketsByStatus(status);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calcule le chiffre d'affaires par statut
     * GET /api/baskets/statistics/revenue-by-status/{status}
     */
    @GetMapping("/statistics/revenue-by-status/{status}")
    public ResponseEntity<BigDecimal> getTotalRevenueByStatus(@PathVariable BasketStatus status) {
        try {
            BigDecimal revenue = basketService.getTotalRevenueByStatus(status);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calcule la valeur moyenne des paniers par statut
     * GET /api/baskets/statistics/average-value-by-status/{status}
     */
    @GetMapping("/statistics/average-value-by-status/{status}")
    public ResponseEntity<BigDecimal> getAverageBasketValueByStatus(@PathVariable BasketStatus status) {
        try {
            BigDecimal averageValue = basketService.getAverageBasketValueByStatus(status);
            return ResponseEntity.ok(averageValue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compte les paniers créés dans une période
     * GET /api/baskets/statistics/count-in-period
     */
    @GetMapping("/statistics/count-in-period")
    public ResponseEntity<Long> countBasketsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Long count = basketService.countBasketsInPeriod(startDate, endDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Calcule les ventes totales dans une période
     * GET /api/baskets/statistics/sales-in-period
     */
    @GetMapping("/statistics/sales-in-period")
    public ResponseEntity<BigDecimal> getTotalSalesInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            BigDecimal totalSales = basketService.getTotalSalesInPeriod(startDate, endDate);
            return ResponseEntity.ok(totalSales);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les meilleurs clients par dépenses
     * GET /api/baskets/statistics/top-customers
     */
    @GetMapping("/statistics/top-customers")
    public ResponseEntity<List<Object[]>> getTopCustomersBySpending() {
        try {
            List<Object[]> topCustomers = basketService.getTopCustomersBySpending();
            return ResponseEntity.ok(topCustomers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== VALIDATION ET VÉRIFICATION ==========

    /**
     * Vérifie si un panier est vide
     * GET /api/baskets/{basketId}/is-empty
     */
    @GetMapping("/{basketId}/is-empty")
    public ResponseEntity<Boolean> isBasketEmpty(@PathVariable Long basketId) {
        try {
            Optional<Basket> basketOpt = basketService.getBasketByIdWithItems(basketId);
            if (basketOpt.isPresent()) {
                return ResponseEntity.ok(basketOpt.get().isEmpty());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vérifie si un panier est actif
     * GET /api/baskets/{basketId}/is-active
     */
    @GetMapping("/{basketId}/is-active")
    public ResponseEntity<Boolean> isBasketActive(@PathVariable Long basketId) {
        try {
            Optional<Basket> basketOpt = basketService.getBasketById(basketId);
            if (basketOpt.isPresent()) {
                return ResponseEntity.ok(basketOpt.get().isActive());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vérifie si un panier contient un produit spécifique
     * GET /api/baskets/{basketId}/contains-product/{productId}
     */
    @GetMapping("/{basketId}/contains-product/{productId}")
    public ResponseEntity<Boolean> containsProduct(@PathVariable Long basketId, @PathVariable Long productId) {
        try {
            Optional<Basket> basketOpt = basketService.getBasketByIdWithItems(basketId);
            if (basketOpt.isPresent()) {
                return ResponseEntity.ok(basketOpt.get().containsProduct(productId));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== GESTION D'ERREURS ET RÉPONSES PERSONNALISÉES ==========

    /**
     * Gestion d'exception pour les paniers introuvables
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("introuvable")) {
            return ResponseEntity.notFound().build();
        } else if (e.getMessage().contains("Stock insuffisant") ||
                e.getMessage().contains("panier vide") ||
                e.getMessage().contains("déjà été")) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne du serveur");
    }
}