package org.ecospace.model.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ecospace.model.SubscriptionType;

import java.time.LocalDateTime;
@NoArgsConstructor
@Getter
@Setter
public class DesignSubscriptionDto {


    private SubscriptionType type;

    private String namePackage;

    private Double price;

    private LocalDateTime createdOn;

    private LocalDateTime expiresOn;


}
