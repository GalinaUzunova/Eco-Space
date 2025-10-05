package org.ecospace.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.YearMonth;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class UserCard  extends BaseEntity{
     @Column(nullable = false)
    private String card;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private YearMonth expiresOn;
    @Column(nullable = false)
    private String cvv;


}
