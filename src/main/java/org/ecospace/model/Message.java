package org.ecospace.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@NoArgsConstructor
@Getter
@Setter
public class Message extends BaseEntity{
    @Column(name = "name",nullable = false)
    private String  userName;
    @Column(nullable = false)
    private String email;

    private String phone;
    @Column(nullable = false,columnDefinition = "BLOB")
    private String message;
}
