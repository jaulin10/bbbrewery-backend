package com.bbbrewery.backend.model;

/**
 * Énumération représentant les différents statuts d'un panier
 * Les valeurs correspondent aux codes stockés en base de données
 */
public enum BasketStatus {
    /**
     * Panier actif (en cours de modification)
     */
    ACTIVE(0, "Actif"),

    /**
     * Panier soumis (en attente de traitement)
     */
    SUBMITTED(1, "Soumis"),

    /**
     * Commande finalisée (checkout effectué)
     */
    CHECKED_OUT(2, "Finalisé"),

    /**
     * Commande en cours de traitement
     */
    PROCESSING(3, "En traitement"),

    /**
     * Commande expédiée
     */
    SHIPPED(4, "Expédié"),

    /**
     * Commande livrée
     */
    DELIVERED(5, "Livré"),

    /**
     * Panier/Commande annulé(e)
     */
    CANCELLED(6, "Annulé"),

    /**
     * Commande remboursée
     */
    REFUNDED(7, "Remboursé");

    private final int code;
    private final String description;

    BasketStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Retourne le code numérique du statut
     */
    public int getCode() {
        return code;
    }

    /**
     * Retourne la description du statut
     */
    public String getDescription() {
        return description;
    }

    /**
     * Trouve un statut par son code numérique
     */
    public static BasketStatus fromCode(int code) {
        for (BasketStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Code de statut invalide: " + code);
    }

    /**
     * Vérifie si le statut indique un panier actif
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Vérifie si le statut indique une commande finalisée
     */
    public boolean isOrdered() {
        return this != ACTIVE;
    }

    /**
     * Vérifie si le statut permet encore des modifications
     */
    public boolean isModifiable() {
        return this == ACTIVE || this == SUBMITTED;
    }

    /**
     * Vérifie si le statut indique une commande en cours de livraison
     */
    public boolean isInShipping() {
        return this == PROCESSING || this == SHIPPED;
    }

    /**
     * Vérifie si le statut indique une commande terminée
     */
    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * Retourne les statuts suivants possibles depuis le statut actuel
     */
    public BasketStatus[] getNextPossibleStatuses() {
        return switch (this) {
            case ACTIVE -> new BasketStatus[]{SUBMITTED, CANCELLED};
            case SUBMITTED -> new BasketStatus[]{CHECKED_OUT, CANCELLED, ACTIVE};
            case CHECKED_OUT -> new BasketStatus[]{PROCESSING, CANCELLED};
            case PROCESSING -> new BasketStatus[]{SHIPPED, CANCELLED};
            case SHIPPED -> new BasketStatus[]{DELIVERED};
            case DELIVERED -> new BasketStatus[]{REFUNDED};
            case CANCELLED, REFUNDED -> new BasketStatus[]{};
        };
    }

    /**
     * Vérifie si une transition vers un autre statut est possible
     */
    public boolean canTransitionTo(BasketStatus newStatus) {
        BasketStatus[] possibleStatuses = getNextPossibleStatuses();
        for (BasketStatus status : possibleStatuses) {
            if (status == newStatus) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return description + " (" + code + ")";
    }
}