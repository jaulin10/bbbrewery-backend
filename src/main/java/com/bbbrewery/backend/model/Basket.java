package com.bbbrewery.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BB_BASKET")
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basket_seq")
    @SequenceGenerator(name = "basket_seq", sequenceName = "BB_IDBASKET_SEQ", allocationSize = 1)
    @Column(name = "IDBASKET")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDSHOPPER", referencedColumnName = "IDSHOPPER")
    private Shopper shopper;

    @Column(name = "DTCREATED")
    private LocalDateTime dateCreated;

    @Column(name = "DTORDERED")
    private LocalDateTime dateOrdered;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "SUBTOTAL", precision = 8, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "SHIPPING", precision = 8, scale = 2)
    private BigDecimal shipping;

    @Column(name = "TAX", precision = 8, scale = 2)
    private BigDecimal tax;

    @Column(name = "TOTAL", precision = 8, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ORDERPLACED")
    private BasketStatus orderPlaced;

    // ========== CHAMPS D'ADRESSE DE LIVRAISON ==========
    @Column(name = "SHIPADDRESS", length = 100)
    private String shipAddress;

    @Column(name = "SHIPCITY", length = 50)
    private String shipCity;

    @Column(name = "SHIPSTATE", length = 2)
    private String shipState;

    @Column(name = "SHIPZIPCODE", length = 10)
    private String shipZipcode;

    @Column(name = "SHIPCOUNTRY", length = 50)
    private String shipCountry;

    // ========== RELATIONS ==========
    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<BasketItem> basketItems = new ArrayList<>();

   // @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //private List<Tax> taxes = new ArrayList<>();

    // Supprimer ou commenter cette relation
/*
    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Shipping> shippings = new ArrayList<>();
    */

    // ========== CONSTRUCTEURS ==========
    public Basket() {
        this.dateCreated = LocalDateTime.now();
        this.orderPlaced = BasketStatus.ACTIVE;
        this.quantity = 0;
        this.subtotal = BigDecimal.ZERO;
        this.shipping = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    public Basket(Shopper shopper) {
        this();
        this.shopper = shopper;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Ajoute un article au panier
     */
    public void addBasketItem(BasketItem item) {
        basketItems.add(item);
        item.setBasket(this);
        updateTotals();
    }

    /**
     * Supprime un article du panier
     */
    public void removeBasketItem(BasketItem item) {
        basketItems.remove(item);
        item.setBasket(null);
        updateTotals();
    }

    /**
     * Met à jour tous les totaux du panier
     */
    public void updateTotals() {
        // Calculer la quantité totale
        this.quantity = basketItems.stream()
                .mapToInt(BasketItem::getQuantity)
                .sum();

        // Calculer le sous-total
        this.subtotal = basketItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculer le total avec taxes et frais de port
        this.total = subtotal
                .add(tax != null ? tax : BigDecimal.ZERO)
                .add(shipping != null ? shipping : BigDecimal.ZERO);
    }

    /**
     * Vérifie si le panier est vide
     */
    public boolean isEmpty() {
        return basketItems == null || basketItems.isEmpty();
    }

    /**
     * Vérifie si le panier est actif
     */
    public boolean isActive() {
        return orderPlaced == BasketStatus.ACTIVE;
    }

    /**
     * Vérifie si le panier a été commandé
     */
    public boolean isOrdered() {
        return orderPlaced != BasketStatus.ACTIVE;
    }

    /**
     * Soumet la commande
     */
    public void submitOrder() {
        if (isActive() && !isEmpty()) {
            this.orderPlaced = BasketStatus.SUBMITTED;
            this.dateOrdered = LocalDateTime.now();
        } else if (isEmpty()) {
            throw new RuntimeException("Impossible de soumettre un panier vide");
        } else {
            throw new RuntimeException("Ce panier a déjà été soumis");
        }
    }

    /**
     * Marque le panier comme finalisé (checkout)
     */
    public void checkout() {
        if (isActive() && !isEmpty()) {
            this.orderPlaced = BasketStatus.CHECKED_OUT;
            this.dateOrdered = LocalDateTime.now();
        } else if (isEmpty()) {
            throw new RuntimeException("Impossible de finaliser un panier vide");
        } else {
            throw new RuntimeException("Ce panier a déjà été finalisé");
        }
    }

    /**
     * Annule le panier
     */
    public void cancel() {
        if (isActive()) {
            this.orderPlaced = BasketStatus.CANCELLED;
            this.dateOrdered = LocalDateTime.now();
        }
    }

    /**
     * Calcule le nombre d'articles uniques
     */
    public int getUniqueItemCount() {
        return basketItems != null ? basketItems.size() : 0;
    }

    /**
     * Trouve un article par ID de produit
     */
    public BasketItem findItemByProductId(Long productId) {
        return basketItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Vérifie si le panier contient un produit
     */
    public boolean containsProduct(Long productId) {
        return findItemByProductId(productId) != null;
    }

    /**
     * Obtient l'adresse de livraison complète
     */
    public String getFullShippingAddress() {
        StringBuilder address = new StringBuilder();
        if (shipAddress != null) address.append(shipAddress);
        if (shipCity != null) address.append(", ").append(shipCity);
        if (shipState != null) address.append(", ").append(shipState);
        if (shipZipcode != null) address.append(" ").append(shipZipcode);
        if (shipCountry != null) address.append(", ").append(shipCountry);
        return address.toString();
    }

    // ========== GETTERS ET SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shopper getShopper() {
        return shopper;
    }

    public void setShopper(Shopper shopper) {
        this.shopper = shopper;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateOrdered() {
        return dateOrdered;
    }

    public void setDateOrdered(LocalDateTime dateOrdered) {
        this.dateOrdered = dateOrdered;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShipping() {
        return shipping;
    }

    public void setShipping(BigDecimal shipping) {
        this.shipping = shipping;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BasketStatus getOrderPlaced() {
        return orderPlaced;
    }

    public void setOrderPlaced(BasketStatus orderPlaced) {
        this.orderPlaced = orderPlaced;
    }

    public String getShipAddress() {
        return shipAddress;
    }

    public void setShipAddress(String shipAddress) {
        this.shipAddress = shipAddress;
    }

    public String getShipCity() {
        return shipCity;
    }

    public void setShipCity(String shipCity) {
        this.shipCity = shipCity;
    }

    public String getShipState() {
        return shipState;
    }

    public void setShipState(String shipState) {
        this.shipState = shipState;
    }

    public String getShipZipcode() {
        return shipZipcode;
    }

    public void setShipZipcode(String shipZipcode) {
        this.shipZipcode = shipZipcode;
    }

    public String getShipCountry() {
        return shipCountry;
    }

    public void setShipCountry(String shipCountry) {
        this.shipCountry = shipCountry;
    }

    public List<BasketItem> getBasketItems() {
        return basketItems;
    }

    public void setBasketItems(List<BasketItem> basketItems) {
        this.basketItems = basketItems;
    }

   // public List<Tax> getTaxes() {
    //    return taxes;
   // }

    //public void setTaxes(List<Tax> taxes) {
    //    this.taxes = taxes;
   // }

    /*
    public List<Shipping> getShippings() {
        return shippings;
    }

    public void setShippings(List<Shipping> shippings) {
        this.shippings = shippings;
    }*/


    @Override
    public String toString() {
        return "Basket{" +
                "id=" + id +
                ", shopper=" + (shopper != null ? shopper.getId() : null) +
                ", dateCreated=" + dateCreated +
                ", orderPlaced=" + orderPlaced +
                ", quantity=" + quantity +
                ", total=" + total +
                ", itemCount=" + getUniqueItemCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Basket)) return false;
        Basket basket = (Basket) o;
        return id != null && id.equals(basket.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}