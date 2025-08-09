package com.bbbrewery.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "BB_SHOPPER")
public class Shopper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopper_seq")
    @SequenceGenerator(name = "shopper_seq", sequenceName = "BB_SHOPID_SEQ", allocationSize = 1)
    @Column(name = "IDSHOPPER")
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 15, message = "First name cannot exceed 15 characters")
    @Column(name = "FIRSTNAME", nullable = false, length = 15)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name cannot exceed 20 characters")
    @Column(name = "LASTNAME", nullable = false, length = 20)
    private String lastName;

    @Email(message = "Email should be valid")
    @Size(max = 25, message = "Email cannot exceed 25 characters")
    @Column(name = "EMAIL", length = 25)
    private String email;

    @Size(max = 10, message = "Phone cannot exceed 10 characters")
    @Column(name = "PHONE", length = 10)
    private String phone;

    @Size(max = 20, message = "Address cannot exceed 20 characters")
    @Column(name = "ADDRESS", length = 20)
    private String address;

    @Size(max = 15, message = "City cannot exceed 15 characters")
    @Column(name = "CITY", length = 15)
    private String city;

    @Size(max = 2, message = "State cannot exceed 2 characters")
    @Column(name = "STATE", length = 2)
    private String state;

    @Size(max = 9, message = "Zip code cannot exceed 9 characters")
    @Column(name = "ZIPCODE", length = 9)
    private String zipCode;

    @Column(name = "DTCREATED")
    private LocalDateTime dateCreated;

    @Column(name = "DTLAST")
    private LocalDateTime dateLastVisit;

    @Column(name = "PROVINCE", length = 15)
    private String province;

    @Column(name = "COUNTRY", length = 15)
    private String country;

    @Column(name = "COOKIE")
    private Integer cookie;

    @OneToMany(mappedBy = "shopper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Basket> baskets;

    // Constructeurs
    public Shopper() {
        this.dateCreated = LocalDateTime.now();
        this.dateLastVisit = LocalDateTime.now();
    }

    public Shopper(String firstName, String lastName, String email) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Méthodes utilitaires
    @PreUpdate
    public void preUpdate() {
        this.dateLastVisit = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (province != null) sb.append(", ").append(province);
        if (zipCode != null) sb.append(" ").append(zipCode);
        if (country != null) sb.append(", ").append(country);
        return sb.toString();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateLastVisit() {
        return dateLastVisit;
    }

    public void setDateLastVisit(LocalDateTime dateLastVisit) {
        this.dateLastVisit = dateLastVisit;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getCookie() {
        return cookie;
    }

    public void setCookie(Integer cookie) {
        this.cookie = cookie;
    }

    public List<Basket> getBaskets() {
        return baskets;
    }

    public void setBaskets(List<Basket> baskets) {
        this.baskets = baskets;
    }

    @Override
    public String toString() {
        return "Shopper{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}