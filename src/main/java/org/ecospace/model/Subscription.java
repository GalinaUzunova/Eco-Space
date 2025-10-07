package org.ecospace.model;

import jakarta.persistence.*;

import lombok.*;


import java.sql.Blob;
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

    @Column( nullable = false ,columnDefinition = "Blob")
    private String description;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "expire")
    private LocalDateTime expiresOn;
    private boolean isActive;



}
