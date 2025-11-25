package org.ecospace.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor

public class Product extends BaseEntity{
@Column(nullable = false)
 private String namePackage;
@Column(nullable = false)
 private String description;
  @Column(nullable = false)
 private Double price;
@Column(nullable = false)
 private    LocalDateTime createdOn;
@Column(nullable = false)
  private   LocalDateTime expired;
@Enumerated(EnumType.STRING)
private SubscriptionType type;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

  private boolean isActive;

  private  boolean isRenewalNotify;

  private boolean isPaid;





}
