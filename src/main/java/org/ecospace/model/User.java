package org.ecospace.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")

public class User  extends BaseEntity{
    @Column(nullable = false,unique = true)
    private String username;


    @Column(nullable = false,unique = true)

    String email;
    @Column(nullable = false)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role;
    @ManyToMany
    private List<Project>clientProjects;

    public User() {
        this.clientProjects=new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<Project> getClientProjects() {
        return clientProjects;
    }

    public void setClientProjects(List<Project> clientProjects) {
        this.clientProjects = clientProjects;
    }
}
