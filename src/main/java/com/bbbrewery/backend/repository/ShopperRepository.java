package com.bbbrewery.backend.repository;

import com.bbbrewery.backend.model.Shopper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShopperRepository extends JpaRepository<Shopper, Long> {

    /**
     * Trouve un client par email
     */
    Optional<Shopper> findByEmail(String email);

    /**
     * Trouve un client par email (insensible à la casse)
     */
    Optional<Shopper> findByEmailIgnoreCase(String email);

    /**
     * Trouve les clients par prénom
     */
    List<Shopper> findByFirstNameIgnoreCase(String firstName);

    /**
     * Trouve les clients par nom de famille
     */
    List<Shopper> findByLastNameIgnoreCase(String lastName);

    /**
     * Trouve les clients par nom complet
     */
    List<Shopper> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    /**
     * Trouve les clients par ville
     */
    List<Shopper> findByCity(String city);

    /**
     * Trouve les clients par état/province
     */
    List<Shopper> findByState(String state);

    /**
     * Trouve les clients par code postal
     */
    List<Shopper> findByZipCode(String zipCode);

    /**
     * Trouve les clients par téléphone
     */
    Optional<Shopper> findByPhone(String phone);

    /**
     * Trouve les clients inscrits dans une période
     */
    List<Shopper> findByDateCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les clients inscrits après une date
     */
    List<Shopper> findByDateCreatedAfter(LocalDateTime date);

    /**
     * Recherche les clients par nom ou prénom
     */
    @Query("SELECT s FROM Shopper s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Shopper> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Trouve les clients par adresse (recherche partielle)
     */
    List<Shopper> findByAddressContainingIgnoreCase(String address);

    /**
     * Compte les clients par état
     */
    @Query("SELECT s.state, COUNT(s) FROM Shopper s GROUP BY s.state ORDER BY COUNT(s) DESC")
    List<Object[]> countShoppersByState();

    /**
     * Compte les clients par ville
     */
    @Query("SELECT s.city, COUNT(s) FROM Shopper s GROUP BY s.city ORDER BY COUNT(s) DESC")
    List<Object[]> countShoppersByCity();

    /**
     * Trouve les clients les plus récents
     */
    List<Shopper> findTop10ByOrderByDateCreatedDesc();

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Vérifie si un téléphone existe déjà
     */
    boolean existsByPhone(String phone);

    /**
     * Compte le total des clients
     */
    @Query("SELECT COUNT(s) FROM Shopper s")
    long getTotalShoppersCount();

    /**
     * Compte les nouveaux clients ce mois
     */
    @Query("SELECT COUNT(s) FROM Shopper s WHERE EXTRACT(YEAR FROM s.dateCreated) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "AND EXTRACT(MONTH FROM s.dateCreated) = EXTRACT(MONTH FROM CURRENT_DATE)")
    long getNewShoppersThisMonth();

    /**
     * Trouve les clients par domaine email
     */
    @Query("SELECT s FROM Shopper s WHERE s.email LIKE CONCAT('%@', :domain)")
    List<Shopper> findByEmailDomain(@Param("domain") String domain);

    /**
     * Statistiques d'inscription par mois
     */
    @Query("SELECT EXTRACT(YEAR FROM s.dateCreated) as year, EXTRACT(MONTH FROM s.dateCreated) as month, COUNT(s) as count " +
            "FROM Shopper s GROUP BY EXTRACT(YEAR FROM s.dateCreated), EXTRACT(MONTH FROM s.dateCreated) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getRegistrationStatistics();

    /**
     * Trouve les clients par pays
     */
    List<Shopper> findByCountry(String country);

    /**
     * Trouve les clients par province
     */
    List<Shopper> findByProvince(String province);

    /**
     * Trouve les clients actifs récemment (dernière visite)
     */
    List<Shopper> findByDateLastVisitAfter(LocalDateTime date);

    /**
     * Trouve les clients inactifs depuis une certaine date
     */
    List<Shopper> findByDateLastVisitBefore(LocalDateTime date);
}