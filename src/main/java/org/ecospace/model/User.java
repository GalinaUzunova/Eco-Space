package org.ecospace.model;

import jakarta.persistence.*;

import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter


@Entity
@Table(name = "users")

public class User  extends BaseEntity {
    @Column(nullable = false,unique = true)
    private String username;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;


    private LocalDateTime createdOn;

    private boolean renew;

    private boolean active;

    private String packageName;

    private LocalDateTime created;

    @ManyToMany()
    private List<Project> clientProjects;
    @ManyToMany(fetch =FetchType.EAGER)
    private List<Subscription>subscriptions;
    @OneToOne
    private UserCard userCard;
}




