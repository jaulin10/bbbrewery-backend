package com.bbbrewery.backend.service;

import com.bbbrewery.backend.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bbbrewery.backend.repository.BasketRepository;
import com.bbbrewery.backend.repository.ProductRepository;
import com.bbbrewery.backend.repository.ShopperRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopperRepository shopperRepository;

    /**
     * Récupère tous les paniers
     */
    @Transactional(readOnly = true)
    public List<Basket> getAllBaskets() {
        return basketRepository.findAll();
    }

    /**
     * Récupère un panier par ID
     */
    @Transactional(readOnly = true)
    public Optional<Basket> getBasketById(Long id) {
        return basketRepository.findById(id);
    }

    /**
     * Récupère un panier avec ses articles par ID (optimisé)
     */
    @Transactional(readOnly = true)
    public Optional<Basket> getBasketByIdWithItems(Long id) {
        return basketRepository.findByIdWithItems(id);
    }

    /**
     * Récupère les paniers d'un client
     */
    @Transactional(readOnly = true)
    public List<Basket> getBasketsByShopperId(Long shopperId) {
        return basketRepository.findByShopperId(shopperId);
    }

    /**
     * Récupère le panier actif d'un client
     */
    @Transactional(readOnly = true)
    public Optional<Basket> getActiveBasketByShopperId(Long shopperId) {
        return basketRepository.findActiveBasketByShopperId(shopperId, BasketStatus.ACTIVE);
    }

    /**
     * Récupère le panier actif avec articles d'un client (optimisé)
     */
    @Transactional(readOnly = true)
    public Optional<Basket> getActiveBasketWithItemsByShopperId(Long shopperId) {
        return basketRepository.findActiveBasketWithItemsByShopperId(shopperId, BasketStatus.ACTIVE);
    }

    /**
     * Récupère les paniers par statut
     */
    @Transactional(readOnly = true)
    public List<Basket> getBasketsByStatus(BasketStatus status) {
        return basketRepository.findByOrderPlaced(status);
    }

    /**
     * Récupère les paniers par période de création
     */
    @Transactional(readOnly = true)
    public List<Basket> getBasketsByDateCreated(LocalDateTime startDate, LocalDateTime endDate) {
        return basketRepository.findByDateCreatedBetween(startDate, endDate);
    }

    /**
     * Récupère les paniers par période de commande
     */
    @Transactional(readOnly = true)
    public List<Basket> getBasketsByDateOrdered(LocalDateTime startDate, LocalDateTime endDate) {
        return basketRepository.findByDateOrderedBetween(startDate, endDate);
    }

    /**
     * Récupère les paniers avec montant minimum
     */
    @Transactional(readOnly = true)
    public List<Basket> getBasketsByMinimumTotal(BigDecimal minAmount) {
        return basketRepository.findBasketsByMinimumTotal(minAmount);
    }

    /**
     * Récupère les paniers récents
     */
    @Transactional(readOnly = true)
    public List<Basket> getRecentBaskets(LocalDateTime since) {
        return basketRepository.findRecentBaskets(since);
    }

    /**
     * Récupère les paniers abandonnés
     */
    @Transactional(readOnly = true)
    public List<Basket> getAbandonedBaskets(LocalDateTime cutoffDate) {
        return basketRepository.findAbandonedBaskets(cutoffDate);
    }

    /**
     * Crée un nouveau panier
     */
    public Basket createBasket(Long shopperId) {
        Shopper shopper = shopperRepository.findById(shopperId)
                .orElseThrow(() -> new RuntimeException("Shopper introuvable avec ID: " + shopperId));

        Basket basket = new Basket(shopper);
        return basketRepository.save(basket);
    }

    /**
     * Met à jour un panier
     */
    public Basket updateBasket(Basket basket) {
        return basketRepository.save(basket);
    }

    /**
     * Met à jour le statut d'un panier
     */
    public Basket updateBasketStatus(Long basketId, BasketStatus status) {
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        basket.setOrderPlaced(status);
        if (status != BasketStatus.ACTIVE) {
            basket.setDateOrdered(LocalDateTime.now());
        }
        return basketRepository.save(basket);
    }

    /**
     * Ajoute un article au panier
     */
    public Basket addItemToBasket(Long basketId, Long productId, int quantity) {
        Basket basket = basketRepository.findByIdWithItems(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable avec ID: " + productId));

        // Vérifier le stock disponible
        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + product.getProductName());
        }

        Optional<BasketItem> existingItem = basket.getBasketItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            BasketItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Vérifier le stock pour la nouvelle quantité
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Stock insuffisant pour la quantité demandée");
            }

            item.setQuantity(newQuantity);
        } else {
            BasketItem newItem = new BasketItem();
            newItem.setBasket(basket);
            newItem.setProduct(product);
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(quantity);
            basket.addBasketItem(newItem);
        }

        basket.updateTotals();
        return basketRepository.save(basket);
    }

    /**
     * Ajoute un article via procédure stockée
     */
    public void addItemViaProcedure(Long basketId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable avec ID: " + productId));

        if (!basketRepository.existsById(basketId)) {
            throw new RuntimeException("Panier introuvable avec ID: " + basketId);
        }

        // Appel de la procédure stockée
        basketRepository.addItemToBasketViaProcedure(basketId, productId, product.getPrice(),
                quantity, null, null);
    }

    /**
     * Met à jour la quantité d'un article
     */
    public Basket updateItemQuantity(Long basketId, Long productId, int newQuantity) {
        Basket basket = basketRepository.findByIdWithItems(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        Optional<BasketItem> itemOpt = basket.getBasketItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            BasketItem item = itemOpt.get();

            if (newQuantity <= 0) {
                basket.removeBasketItem(item);
            } else {
                // Vérifier le stock disponible
                if (item.getProduct().getStock() < newQuantity) {
                    throw new RuntimeException("Stock insuffisant pour la quantité demandée");
                }
                item.setQuantity(newQuantity);
            }

            basket.updateTotals();
            return basketRepository.save(basket);
        }

        throw new RuntimeException("Article introuvable dans le panier");
    }

    /**
     * Supprime un article du panier
     */
    public Basket removeItemFromBasket(Long basketId, Long productId) {
        return updateItemQuantity(basketId, productId, 0);
    }

    /**
     * Vide le panier
     */
    public Basket clearBasket(Long basketId) {
        Basket basket = basketRepository.findByIdWithItems(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        basket.getBasketItems().clear();
        basket.updateTotals();
        return basketRepository.save(basket);
    }

    /**
     * Finalise une commande (checkout)
     */
    public Basket checkoutBasket(Long basketId) {
        Basket basket = basketRepository.findByIdWithItems(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        if (basket.isEmpty()) {
            throw new RuntimeException("Impossible de finaliser un panier vide");
        }

        if (!basket.isActive()) {
            throw new RuntimeException("Ce panier a déjà été finalisé");
        }

        // Vérifier le stock pour tous les articles
        for (BasketItem item : basket.getBasketItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour le produit: " + product.getProductName());
            }
        }

        basket.submitOrder();
        return basketRepository.save(basket);
    }

    /**
     * Met à jour les taxes du panier
     */
    public Basket updateBasketTax(Long basketId, BigDecimal tax) {
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        basket.setTax(tax);
        basket.updateTotals();
        return basketRepository.save(basket);
    }

    /**
     * Met à jour les frais de port du panier
     */
    public Basket updateBasketShipping(Long basketId, BigDecimal shipping) {
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        basket.setShipping(shipping);
        basket.updateTotals();
        return basketRepository.save(basket);
    }

    /**
     * Compte le nombre d'articles dans le panier
     */
    @Transactional(readOnly = true)
    public int getItemCount(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));
        return basket.getQuantity();
    }

    /**
     * Calcule le nombre total d'articles (somme des quantités)
     */
    @Transactional(readOnly = true)
    public int getTotalItemQuantity(Long basketId) {
        Basket basket = basketRepository.findByIdWithItems(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        return basket.getBasketItems().stream()
                .mapToInt(BasketItem::getQuantity)
                .sum();
    }

    /**
     * Récupère le total des achats d'un client via fonction stockée
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPurchasesByShopperId(Long shopperId) {
        return basketRepository.getTotalPurchasesByShopperId(shopperId);
    }

    /**
     * Vérifie si un produit est en promotion via fonction stockée
     */
    @Transactional(readOnly = true)
    public boolean checkProductOnSale(Long productId) {
        Integer result = basketRepository.checkProductOnSale(productId);
        return result != null && result == 1;
    }

    // ========== MÉTHODES STATISTIQUES ==========

    /**
     * Compte les paniers par statut
     */
    @Transactional(readOnly = true)
    public Long countBasketsByStatus(BasketStatus status) {
        return basketRepository.countBasketsByStatus(status);
    }

    /**
     * Calcule le chiffre d'affaires par statut
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueByStatus(BasketStatus status) {
        return basketRepository.getTotalRevenueByStatus(status);
    }

    /**
     * Calcule la valeur moyenne des paniers par statut
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageBasketValueByStatus(BasketStatus status) {
        return basketRepository.getAverageBasketValueByStatus(status);
    }

    /**
     * Compte les paniers créés dans une période
     */
    @Transactional(readOnly = true)
    public Long countBasketsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return basketRepository.countBasketsInPeriod(startDate, endDate);
    }

    /**
     * Calcule les ventes totales dans une période
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalSalesInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return basketRepository.getTotalSalesInPeriod(startDate, endDate);
    }

    /**
     * Récupère les meilleurs clients par dépenses
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTopCustomersBySpending() {
        return basketRepository.getTopCustomersBySpending();
    }

    /**
     * Supprime un panier (avec vérifications)
     */
    public void deleteBasket(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable avec ID: " + basketId));

        if (basket.isOrdered()) {
            throw new RuntimeException("Impossible de supprimer un panier déjà commandé");
        }

        basketRepository.deleteById(basketId);
    }
}