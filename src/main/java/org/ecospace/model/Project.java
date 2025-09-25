package org.ecospace.model;

import jakarta.persistence.*;


import java.time.LocalDate;

@Entity
@Table(name = "projects")
public class Project extends BaseEntity{
 @Column(nullable = false)
    private String name;
 @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate dueDate;
 @Column(nullable = false)
    private String location;

    private String description;

    private String status;
    @Enumerated(EnumType.STRING)
    private SubscriptionType type;
//  @ManyToOne
//    private User client;

    public Project() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

//    public User getClient() {
//        return client;
//    }
//
//    public void setClient(User client) {
//        this.client = client;
//    }
}
