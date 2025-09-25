package org.ecospace.model;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "subscription")
public class Subscription extends BaseEntity {


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;
    @Column(nullable = false)
    private String namePackage;
    @Column(nullable = false)
    private String price;

    private LocalDate createdOn;

    private String description;
    @ManyToOne
    private User client;

    public Subscription() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

    public String getNamePackage() {
        return namePackage;
    }

    public void setNamePackage(String namePackige) {
        this.namePackage = namePackige;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public User getClient() {
        return client;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDate createdOn) {
        this.createdOn = createdOn;
    }

    public void setClient(User client) {
        this.client = client;
    }
}
