package org.ecospace.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;


import java.util.UUID;




    @MappedSuperclass
    public abstract class BaseEntity {

        @Id
        @GeneratedValue
        @UuidGenerator
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

