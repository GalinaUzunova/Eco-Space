package org.ecospace.model;

import jakarta.persistence.*;

import lombok.*;


import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "subscription")
public class Subscription extends BaseEntity {


    @Enumerated(EnumType.STRING)
    private SubscriptionType type;
    @Column(nullable = false)
    private String namePackage;
    @Column(nullable = false)
    private Double price;
    @Column(name = "data_created")
    private LocalDateTime createdOn;
    @Column(name = "expire_date")
    private LocalDateTime expiresOn;

      @Column(name = "subscription_period")
     private SubscriptionPeriod subscriptionPeriod;

    private boolean isActive;
    @ManyToOne
    private User client;


}
