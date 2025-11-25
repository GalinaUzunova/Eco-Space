package org.ecospace.model;

import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false)
    private String username;
    @Email
    @Column(nullable = false)
    String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String phone;
    private LocalDateTime createdOn;
    private String image;
    private boolean active;
    private boolean isNotified;

    @Column(name = "pending_order_id")
    private String pendingPaymentOrderId;
    @Column(name = "pending_sub_order_id")
    private UUID setPendingSubscriptionId;

    @OneToMany
    private List<Subscription> subscriptions;
    @OneToMany(fetch = FetchType.EAGER)
    private List<Product> productList;

}




