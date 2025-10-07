package org.ecospace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project extends BaseEntity{
 @Column(nullable = false)
    private String name;
 @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime dueDate;
 @Column(nullable = false)
    private String location;

    private String description;

    private String status;

    private String type;




}
