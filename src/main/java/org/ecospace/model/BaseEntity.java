package org.ecospace.model;

import jakarta.persistence.*;

import java.util.UUID;


@MappedSuperclass
public  abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)


    private UUID id;

    public BaseEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
